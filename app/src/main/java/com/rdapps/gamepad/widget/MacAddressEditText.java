package com.rdapps.gamepad.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

/**
 * Modified version of <a href="https://github.com/r-cohen/macaddress-edittext">Mac Address EditText</a>
 */
public class MacAddressEditText extends androidx.appcompat.widget.AppCompatEditText {
    String mPreviousMac = null;

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
            private void setMacEdit(String cleanMac, String formattedMac, int selectionStart, int lengthDiff) {
                MacAddressEditText.this.removeTextChangedListener(this);
                if (cleanMac.length() <= 12) {
                    MacAddressEditText.this.setText(formattedMac);
                    MacAddressEditText.this.setSelection(selectionStart + lengthDiff);
                    mPreviousMac = formattedMac;
                } else {
                    MacAddressEditText.this.setText(mPreviousMac);
                    MacAddressEditText.this.setSelection(mPreviousMac.length());
                }
                MacAddressEditText.this.addTextChangedListener(this);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String enteredMac = MacAddressEditText.this.getText().toString().toUpperCase();
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
        if (mPreviousMac != null && mPreviousMac.length() > 1) {
            int previousColonCount = colonCount(mPreviousMac);
            int currentColonCount = colonCount(enteredMac);

            if (currentColonCount < previousColonCount) {
                formattedMac = formattedMac.substring(0, selectionStart - 1) + formattedMac.substring(selectionStart);
                String cleanMac = clearNonMacCharacters(formattedMac);
                formattedMac = formatMacAddress(cleanMac);
            }
        }

        return formattedMac;
    }

    private static String formatMacAddress(String cleanMac) {
        int groupedCharacters = 0;
        String formattedMac = "";

        for (int i = 0; i < cleanMac.length(); ++i) {
            formattedMac += cleanMac.charAt(i);
            ++groupedCharacters;
            if (groupedCharacters == 2) {
                formattedMac += ":";
                groupedCharacters = 0;
            }
        }

        if (cleanMac.length() == 12) {
            formattedMac = formattedMac.substring(0, formattedMac.length() - 1);
        }

        return formattedMac;
    }

    private static String clearNonMacCharacters(String mac) {
        return mac.replaceAll("[^A-Fa-f0-9]", "");
    }

    private static int colonCount(String formattedMac) {
        return formattedMac.replaceAll("[^:]", "").length();
    }
}