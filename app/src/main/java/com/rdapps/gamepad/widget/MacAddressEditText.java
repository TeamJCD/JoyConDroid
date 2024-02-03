package com.rdapps.gamepad.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import java.util.Locale;

/**
 * Modified version of <a href="https://github.com/r-cohen/macaddress-edittext">Mac Address EditText</a>
 */
public class MacAddressEditText extends androidx.appcompat.widget.AppCompatEditText {
    String previousMac = null;

    public MacAddressEditText(Context context) {
        super(context);
        init();
    }

    public MacAddressEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MacAddressEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        this.addTextChangedListener(new TextWatcher() {
            private void setMacEdit(
                    CharSequence cleanMac, String formattedMac,
                    int selectionStart, int lengthDiff) {
                MacAddressEditText.this.removeTextChangedListener(this);
                if (cleanMac.length() <= 12) {
                    MacAddressEditText.this.setText(formattedMac);
                    MacAddressEditText.this.setSelection(selectionStart + lengthDiff);
                    previousMac = formattedMac;
                } else {
                    MacAddressEditText.this.setText(previousMac);
                    MacAddressEditText.this.setSelection(previousMac.length());
                }
                MacAddressEditText.this.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String enteredMac = MacAddressEditText.this.getText().toString()
                        .toUpperCase(Locale.ROOT);
                String cleanMac = clearNonMacCharacters(enteredMac);

                int selectionStart = MacAddressEditText.this.getSelectionStart();

                String formattedMac = formatMacAddress(cleanMac);
                formattedMac = handleColonDeletion(enteredMac, formattedMac, selectionStart);

                int lengthDiff = formattedMac.length() - enteredMac.length();

                setMacEdit(cleanMac, formattedMac, selectionStart, lengthDiff);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private String handleColonDeletion(String enteredMac, String formattedMac, int selectionStart) {
        String result = formattedMac;
        if (previousMac != null && previousMac.length() > 1) {
            int previousColonCount = colonCount(previousMac);
            int currentColonCount = colonCount(enteredMac);

            if (currentColonCount < previousColonCount) {
                result = result.substring(0, selectionStart - 1) + result.substring(selectionStart);
                String cleanMac = clearNonMacCharacters(result);
                result = formatMacAddress(cleanMac);
            }
        }

        return result;
    }

    private static String formatMacAddress(CharSequence cleanMac) {
        int groupedCharacters = 0;
        StringBuilder formattedMac = new StringBuilder();

        for (int i = 0; i < cleanMac.length(); ++i) {
            formattedMac.append(cleanMac.charAt(i));
            ++groupedCharacters;
            if (groupedCharacters == 2) {
                formattedMac.append(":");
                groupedCharacters = 0;
            }
        }

        if (cleanMac.length() == 12) {
            formattedMac = new StringBuilder(formattedMac.substring(0, formattedMac.length() - 1));
        }

        return formattedMac.toString();
    }

    private static String clearNonMacCharacters(String mac) {
        return mac.replaceAll("[^A-Fa-f0-9]", "");
    }

    private static int colonCount(String formattedMac) {
        return formattedMac.replaceAll("[^:]", "").length();
    }
}
