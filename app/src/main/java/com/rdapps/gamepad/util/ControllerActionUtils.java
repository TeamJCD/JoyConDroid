package com.rdapps.gamepad.util;

import android.content.Context;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rdapps.gamepad.device.ButtonType;
import com.rdapps.gamepad.device.JoystickType;
import com.rdapps.gamepad.model.ControllerAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


import static android.view.KeyEvent.*;
import static android.view.MotionEvent.*;
import static com.rdapps.gamepad.device.ButtonType.*;
import static com.rdapps.gamepad.device.JoystickType.LEFT_JOYSTICK;
import static com.rdapps.gamepad.device.JoystickType.RIGHT_JOYSTICK;
import static com.rdapps.gamepad.model.ControllerAction.Type.AXIS;
import static com.rdapps.gamepad.model.ControllerAction.Type.BUTTON;
import static com.rdapps.gamepad.model.ControllerAction.Type.JOYSTICK;

public class ControllerActionUtils {
    public static Map<Integer, String> BUTTON_NAMES = new HashMap();

    static {
        BUTTON_NAMES.put(0, "KEYCODE_UNKNOWN");
        BUTTON_NAMES.put(1, "KEYCODE_SOFT_LEFT");
        BUTTON_NAMES.put(2, "KEYCODE_SOFT_RIGHT");
        BUTTON_NAMES.put(3, "KEYCODE_HOME");
        BUTTON_NAMES.put(4, "KEYCODE_BACK");
        BUTTON_NAMES.put(5, "KEYCODE_CALL");
        BUTTON_NAMES.put(6, "KEYCODE_ENDCALL");
        BUTTON_NAMES.put(7, "KEYCODE_0");
        BUTTON_NAMES.put(8, "KEYCODE_1");
        BUTTON_NAMES.put(9, "KEYCODE_2");
        BUTTON_NAMES.put(10, "KEYCODE_3");
        BUTTON_NAMES.put(11, "KEYCODE_4");
        BUTTON_NAMES.put(12, "KEYCODE_5");
        BUTTON_NAMES.put(13, "KEYCODE_6");
        BUTTON_NAMES.put(14, "KEYCODE_7");
        BUTTON_NAMES.put(15, "KEYCODE_8");
        BUTTON_NAMES.put(16, "KEYCODE_9");
        BUTTON_NAMES.put(17, "KEYCODE_STAR");
        BUTTON_NAMES.put(18, "KEYCODE_POUND");
        BUTTON_NAMES.put(19, "KEYCODE_DPAD_UP");
        BUTTON_NAMES.put(20, "KEYCODE_DPAD_DOWN");
        BUTTON_NAMES.put(21, "KEYCODE_DPAD_LEFT");
        BUTTON_NAMES.put(22, "KEYCODE_DPAD_RIGHT");
        BUTTON_NAMES.put(23, "KEYCODE_DPAD_CENTER");
        BUTTON_NAMES.put(24, "KEYCODE_VOLUME_UP");
        BUTTON_NAMES.put(25, "KEYCODE_VOLUME_DOWN");
        BUTTON_NAMES.put(26, "KEYCODE_POWER");
        BUTTON_NAMES.put(27, "KEYCODE_CAMERA");
        BUTTON_NAMES.put(28, "KEYCODE_CLEAR");
        BUTTON_NAMES.put(29, "KEYCODE_A");
        BUTTON_NAMES.put(30, "KEYCODE_B");
        BUTTON_NAMES.put(31, "KEYCODE_C");
        BUTTON_NAMES.put(32, "KEYCODE_D");
        BUTTON_NAMES.put(33, "KEYCODE_E");
        BUTTON_NAMES.put(34, "KEYCODE_F");
        BUTTON_NAMES.put(35, "KEYCODE_G");
        BUTTON_NAMES.put(36, "KEYCODE_H");
        BUTTON_NAMES.put(37, "KEYCODE_I");
        BUTTON_NAMES.put(38, "KEYCODE_J");
        BUTTON_NAMES.put(39, "KEYCODE_K");
        BUTTON_NAMES.put(40, "KEYCODE_L");
        BUTTON_NAMES.put(41, "KEYCODE_M");
        BUTTON_NAMES.put(42, "KEYCODE_N");
        BUTTON_NAMES.put(43, "KEYCODE_O");
        BUTTON_NAMES.put(44, "KEYCODE_P");
        BUTTON_NAMES.put(45, "KEYCODE_Q");
        BUTTON_NAMES.put(46, "KEYCODE_R");
        BUTTON_NAMES.put(47, "KEYCODE_S");
        BUTTON_NAMES.put(48, "KEYCODE_T");
        BUTTON_NAMES.put(49, "KEYCODE_U");
        BUTTON_NAMES.put(50, "KEYCODE_V");
        BUTTON_NAMES.put(51, "KEYCODE_W");
        BUTTON_NAMES.put(52, "KEYCODE_X");
        BUTTON_NAMES.put(53, "KEYCODE_Y");
        BUTTON_NAMES.put(54, "KEYCODE_Z");
        BUTTON_NAMES.put(55, "KEYCODE_COMMA");
        BUTTON_NAMES.put(56, "KEYCODE_PERIOD");
        BUTTON_NAMES.put(57, "KEYCODE_ALT_LEFT");
        BUTTON_NAMES.put(58, "KEYCODE_ALT_RIGHT");
        BUTTON_NAMES.put(59, "KEYCODE_SHIFT_LEFT");
        BUTTON_NAMES.put(60, "KEYCODE_SHIFT_RIGHT");
        BUTTON_NAMES.put(61, "KEYCODE_TAB");
        BUTTON_NAMES.put(62, "KEYCODE_SPACE");
        BUTTON_NAMES.put(63, "KEYCODE_SYM");
        BUTTON_NAMES.put(64, "KEYCODE_EXPLORER");
        BUTTON_NAMES.put(65, "KEYCODE_ENVELOPE");
        BUTTON_NAMES.put(66, "KEYCODE_ENTER");
        BUTTON_NAMES.put(67, "KEYCODE_DEL");
        BUTTON_NAMES.put(68, "KEYCODE_GRAVE");
        BUTTON_NAMES.put(69, "KEYCODE_MINUS");
        BUTTON_NAMES.put(70, "KEYCODE_EQUALS");
        BUTTON_NAMES.put(71, "KEYCODE_LEFT_BRACKET");
        BUTTON_NAMES.put(72, "KEYCODE_RIGHT_BRACKET");
        BUTTON_NAMES.put(73, "KEYCODE_BACKSLASH");
        BUTTON_NAMES.put(74, "KEYCODE_SEMICOLON");
        BUTTON_NAMES.put(75, "KEYCODE_APOSTROPHE");
        BUTTON_NAMES.put(76, "KEYCODE_SLASH");
        BUTTON_NAMES.put(77, "KEYCODE_AT");
        BUTTON_NAMES.put(78, "KEYCODE_NUM");
        BUTTON_NAMES.put(79, "KEYCODE_HEADSETHOOK");
        BUTTON_NAMES.put(80, "KEYCODE_FOCUS");
        BUTTON_NAMES.put(81, "KEYCODE_PLUS");
        BUTTON_NAMES.put(82, "KEYCODE_MENU");
        BUTTON_NAMES.put(83, "KEYCODE_NOTIFICATION");
        BUTTON_NAMES.put(84, "KEYCODE_SEARCH");
        BUTTON_NAMES.put(85, "KEYCODE_MEDIA_PLAY_PAUSE");
        BUTTON_NAMES.put(86, "KEYCODE_MEDIA_STOP");
        BUTTON_NAMES.put(87, "KEYCODE_MEDIA_NEXT");
        BUTTON_NAMES.put(88, "KEYCODE_MEDIA_PREVIOUS");
        BUTTON_NAMES.put(89, "KEYCODE_MEDIA_REWIND");
        BUTTON_NAMES.put(90, "KEYCODE_MEDIA_FAST_FORWARD");
        BUTTON_NAMES.put(91, "KEYCODE_MUTE");
        BUTTON_NAMES.put(92, "KEYCODE_PAGE_UP");
        BUTTON_NAMES.put(93, "KEYCODE_PAGE_DOWN");
        BUTTON_NAMES.put(94, "KEYCODE_PICTSYMBOLS");
        BUTTON_NAMES.put(95, "KEYCODE_SWITCH_CHARSET");
        BUTTON_NAMES.put(96, "KEYCODE_BUTTON_A");
        BUTTON_NAMES.put(97, "KEYCODE_BUTTON_B");
        BUTTON_NAMES.put(98, "KEYCODE_BUTTON_C");
        BUTTON_NAMES.put(99, "KEYCODE_BUTTON_X");
        BUTTON_NAMES.put(100, "KEYCODE_BUTTON_Y");
        BUTTON_NAMES.put(101, "KEYCODE_BUTTON_Z");
        BUTTON_NAMES.put(102, "KEYCODE_BUTTON_L1");
        BUTTON_NAMES.put(103, "KEYCODE_BUTTON_R1");
        BUTTON_NAMES.put(104, "KEYCODE_BUTTON_L2");
        BUTTON_NAMES.put(105, "KEYCODE_BUTTON_R2");
        BUTTON_NAMES.put(106, "KEYCODE_BUTTON_THUMBL");
        BUTTON_NAMES.put(107, "KEYCODE_BUTTON_THUMBR");
        BUTTON_NAMES.put(108, "KEYCODE_BUTTON_START");
        BUTTON_NAMES.put(109, "KEYCODE_BUTTON_SELECT");
        BUTTON_NAMES.put(110, "KEYCODE_BUTTON_MODE");
        BUTTON_NAMES.put(111, "KEYCODE_ESCAPE");
        BUTTON_NAMES.put(112, "KEYCODE_FORWARD_DEL");
        BUTTON_NAMES.put(113, "KEYCODE_CTRL_LEFT");
        BUTTON_NAMES.put(114, "KEYCODE_CTRL_RIGHT");
        BUTTON_NAMES.put(115, "KEYCODE_CAPS_LOCK");
        BUTTON_NAMES.put(116, "KEYCODE_SCROLL_LOCK");
        BUTTON_NAMES.put(117, "KEYCODE_META_LEFT");
        BUTTON_NAMES.put(118, "KEYCODE_META_RIGHT");
        BUTTON_NAMES.put(119, "KEYCODE_FUNCTION");
        BUTTON_NAMES.put(120, "KEYCODE_SYSRQ");
        BUTTON_NAMES.put(121, "KEYCODE_BREAK");
        BUTTON_NAMES.put(122, "KEYCODE_MOVE_HOME");
        BUTTON_NAMES.put(123, "KEYCODE_MOVE_END");
        BUTTON_NAMES.put(124, "KEYCODE_INSERT");
        BUTTON_NAMES.put(125, "KEYCODE_FORWARD");
        BUTTON_NAMES.put(126, "KEYCODE_MEDIA_PLAY");
        BUTTON_NAMES.put(127, "KEYCODE_MEDIA_PAUSE");
        BUTTON_NAMES.put(128, "KEYCODE_MEDIA_CLOSE");
        BUTTON_NAMES.put(129, "KEYCODE_MEDIA_EJECT");
        BUTTON_NAMES.put(130, "KEYCODE_MEDIA_RECORD");
        BUTTON_NAMES.put(131, "KEYCODE_F1");
        BUTTON_NAMES.put(132, "KEYCODE_F2");
        BUTTON_NAMES.put(133, "KEYCODE_F3");
        BUTTON_NAMES.put(134, "KEYCODE_F4");
        BUTTON_NAMES.put(135, "KEYCODE_F5");
        BUTTON_NAMES.put(136, "KEYCODE_F6");
        BUTTON_NAMES.put(137, "KEYCODE_F7");
        BUTTON_NAMES.put(138, "KEYCODE_F8");
        BUTTON_NAMES.put(139, "KEYCODE_F9");
        BUTTON_NAMES.put(140, "KEYCODE_F10");
        BUTTON_NAMES.put(141, "KEYCODE_F11");
        BUTTON_NAMES.put(142, "KEYCODE_F12");
        BUTTON_NAMES.put(143, "KEYCODE_NUM_LOCK");
        BUTTON_NAMES.put(144, "KEYCODE_NUMPAD_0");
        BUTTON_NAMES.put(145, "KEYCODE_NUMPAD_1");
        BUTTON_NAMES.put(146, "KEYCODE_NUMPAD_2");
        BUTTON_NAMES.put(147, "KEYCODE_NUMPAD_3");
        BUTTON_NAMES.put(148, "KEYCODE_NUMPAD_4");
        BUTTON_NAMES.put(149, "KEYCODE_NUMPAD_5");
        BUTTON_NAMES.put(150, "KEYCODE_NUMPAD_6");
        BUTTON_NAMES.put(151, "KEYCODE_NUMPAD_7");
        BUTTON_NAMES.put(152, "KEYCODE_NUMPAD_8");
        BUTTON_NAMES.put(153, "KEYCODE_NUMPAD_9");
        BUTTON_NAMES.put(154, "KEYCODE_NUMPAD_DIVIDE");
        BUTTON_NAMES.put(155, "KEYCODE_NUMPAD_MULTIPLY");
        BUTTON_NAMES.put(156, "KEYCODE_NUMPAD_SUBTRACT");
        BUTTON_NAMES.put(157, "KEYCODE_NUMPAD_ADD");
        BUTTON_NAMES.put(158, "KEYCODE_NUMPAD_DOT");
        BUTTON_NAMES.put(159, "KEYCODE_NUMPAD_COMMA");
        BUTTON_NAMES.put(160, "KEYCODE_NUMPAD_ENTER");
        BUTTON_NAMES.put(161, "KEYCODE_NUMPAD_EQUALS");
        BUTTON_NAMES.put(162, "KEYCODE_NUMPAD_LEFT_PAREN");
        BUTTON_NAMES.put(163, "KEYCODE_NUMPAD_RIGHT_PAREN");
        BUTTON_NAMES.put(164, "KEYCODE_VOLUME_MUTE");
        BUTTON_NAMES.put(165, "KEYCODE_INFO");
        BUTTON_NAMES.put(166, "KEYCODE_CHANNEL_UP");
        BUTTON_NAMES.put(167, "KEYCODE_CHANNEL_DOWN");
        BUTTON_NAMES.put(168, "KEYCODE_ZOOM_IN");
        BUTTON_NAMES.put(169, "KEYCODE_ZOOM_OUT");
        BUTTON_NAMES.put(170, "KEYCODE_TV");
        BUTTON_NAMES.put(171, "KEYCODE_WINDOW");
        BUTTON_NAMES.put(172, "KEYCODE_GUIDE");
        BUTTON_NAMES.put(173, "KEYCODE_DVR");
        BUTTON_NAMES.put(174, "KEYCODE_BOOKMARK");
        BUTTON_NAMES.put(175, "KEYCODE_CAPTIONS");
        BUTTON_NAMES.put(176, "KEYCODE_SETTINGS");
        BUTTON_NAMES.put(177, "KEYCODE_TV_POWER");
        BUTTON_NAMES.put(178, "KEYCODE_TV_INPUT");
        BUTTON_NAMES.put(179, "KEYCODE_STB_POWER");
        BUTTON_NAMES.put(180, "KEYCODE_STB_INPUT");
        BUTTON_NAMES.put(181, "KEYCODE_AVR_POWER");
        BUTTON_NAMES.put(182, "KEYCODE_AVR_INPUT");
        BUTTON_NAMES.put(183, "KEYCODE_PROG_RED");
        BUTTON_NAMES.put(184, "KEYCODE_PROG_GREEN");
        BUTTON_NAMES.put(185, "KEYCODE_PROG_YELLOW");
        BUTTON_NAMES.put(186, "KEYCODE_PROG_BLUE");
        BUTTON_NAMES.put(187, "KEYCODE_APP_SWITCH");
        BUTTON_NAMES.put(188, "KEYCODE_BUTTON_1");
        BUTTON_NAMES.put(189, "KEYCODE_BUTTON_2");
        BUTTON_NAMES.put(190, "KEYCODE_BUTTON_3");
        BUTTON_NAMES.put(191, "KEYCODE_BUTTON_4");
        BUTTON_NAMES.put(192, "KEYCODE_BUTTON_5");
        BUTTON_NAMES.put(193, "KEYCODE_BUTTON_6");
        BUTTON_NAMES.put(194, "KEYCODE_BUTTON_7");
        BUTTON_NAMES.put(195, "KEYCODE_BUTTON_8");
        BUTTON_NAMES.put(196, "KEYCODE_BUTTON_9");
        BUTTON_NAMES.put(197, "KEYCODE_BUTTON_10");
        BUTTON_NAMES.put(198, "KEYCODE_BUTTON_11");
        BUTTON_NAMES.put(199, "KEYCODE_BUTTON_12");
        BUTTON_NAMES.put(200, "KEYCODE_BUTTON_13");
        BUTTON_NAMES.put(201, "KEYCODE_BUTTON_14");
        BUTTON_NAMES.put(202, "KEYCODE_BUTTON_15");
        BUTTON_NAMES.put(203, "KEYCODE_BUTTON_16");
        BUTTON_NAMES.put(204, "KEYCODE_LANGUAGE_SWITCH");
        BUTTON_NAMES.put(205, "KEYCODE_MANNER_MODE");
        BUTTON_NAMES.put(206, "KEYCODE_3D_MODE");
        BUTTON_NAMES.put(207, "KEYCODE_CONTACTS");
        BUTTON_NAMES.put(208, "KEYCODE_CALENDAR");
        BUTTON_NAMES.put(209, "KEYCODE_MUSIC");
        BUTTON_NAMES.put(210, "KEYCODE_CALCULATOR");
        BUTTON_NAMES.put(211, "KEYCODE_ZENKAKU_HANKAKU");
        BUTTON_NAMES.put(212, "KEYCODE_EISU");
        BUTTON_NAMES.put(213, "KEYCODE_MUHENKAN");
        BUTTON_NAMES.put(214, "KEYCODE_HENKAN");
        BUTTON_NAMES.put(215, "KEYCODE_KATAKANA_HIRAGANA");
        BUTTON_NAMES.put(216, "KEYCODE_YEN");
        BUTTON_NAMES.put(217, "KEYCODE_RO");
        BUTTON_NAMES.put(218, "KEYCODE_KANA");
        BUTTON_NAMES.put(219, "KEYCODE_ASSIST");
        BUTTON_NAMES.put(220, "KEYCODE_BRIGHTNESS_DOWN");
        BUTTON_NAMES.put(221, "KEYCODE_BRIGHTNESS_UP");
        BUTTON_NAMES.put(222, "KEYCODE_MEDIA_AUDIO_TRACK");
        BUTTON_NAMES.put(223, "KEYCODE_SLEEP");
        BUTTON_NAMES.put(224, "KEYCODE_WAKEUP");
        BUTTON_NAMES.put(225, "KEYCODE_PAIRING");
        BUTTON_NAMES.put(226, "KEYCODE_MEDIA_TOP_MENU");
        BUTTON_NAMES.put(227, "KEYCODE_11");
        BUTTON_NAMES.put(228, "KEYCODE_12");
        BUTTON_NAMES.put(229, "KEYCODE_LAST_CHANNEL");
        BUTTON_NAMES.put(230, "KEYCODE_TV_DATA_SERVICE");
        BUTTON_NAMES.put(231, "KEYCODE_VOICE_ASSIST");
        BUTTON_NAMES.put(232, "KEYCODE_TV_RADIO_SERVICE");
        BUTTON_NAMES.put(233, "KEYCODE_TV_TELETEXT");
        BUTTON_NAMES.put(234, "KEYCODE_TV_NUMBER_ENTRY");
        BUTTON_NAMES.put(235, "KEYCODE_TV_TERRESTRIAL_ANALOG");
        BUTTON_NAMES.put(236, "KEYCODE_TV_TERRESTRIAL_DIGITAL");
        BUTTON_NAMES.put(237, "KEYCODE_TV_SATELLITE");
        BUTTON_NAMES.put(238, "KEYCODE_TV_SATELLITE_BS");
        BUTTON_NAMES.put(239, "KEYCODE_TV_SATELLITE_CS");
        BUTTON_NAMES.put(240, "KEYCODE_TV_SATELLITE_SERVICE");
        BUTTON_NAMES.put(241, "KEYCODE_TV_NETWORK");
        BUTTON_NAMES.put(242, "KEYCODE_TV_ANTENNA_CABLE");
        BUTTON_NAMES.put(243, "KEYCODE_TV_INPUT_HDMI_1");
        BUTTON_NAMES.put(244, "KEYCODE_TV_INPUT_HDMI_2");
        BUTTON_NAMES.put(245, "KEYCODE_TV_INPUT_HDMI_3");
        BUTTON_NAMES.put(246, "KEYCODE_TV_INPUT_HDMI_4");
        BUTTON_NAMES.put(247, "KEYCODE_TV_INPUT_COMPOSITE_1");
        BUTTON_NAMES.put(248, "KEYCODE_TV_INPUT_COMPOSITE_2");
        BUTTON_NAMES.put(249, "KEYCODE_TV_INPUT_COMPONENT_1");
        BUTTON_NAMES.put(250, "KEYCODE_TV_INPUT_COMPONENT_2");
        BUTTON_NAMES.put(251, "KEYCODE_TV_INPUT_VGA_1");
        BUTTON_NAMES.put(252, "KEYCODE_TV_AUDIO_DESCRIPTION");
        BUTTON_NAMES.put(253, "KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP");
        BUTTON_NAMES.put(254, "KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN");
        BUTTON_NAMES.put(255, "KEYCODE_TV_ZOOM_MODE");
        BUTTON_NAMES.put(256, "KEYCODE_TV_CONTENTS_MENU");
        BUTTON_NAMES.put(257, "KEYCODE_TV_MEDIA_CONTEXT_MENU");
        BUTTON_NAMES.put(258, "KEYCODE_TV_TIMER_PROGRAMMING");
        BUTTON_NAMES.put(259, "KEYCODE_HELP");
        BUTTON_NAMES.put(260, "KEYCODE_NAVIGATE_PREVIOUS");
        BUTTON_NAMES.put(261, "KEYCODE_NAVIGATE_NEXT");
        BUTTON_NAMES.put(262, "KEYCODE_NAVIGATE_IN");
        BUTTON_NAMES.put(263, "KEYCODE_NAVIGATE_OUT");
        BUTTON_NAMES.put(264, "KEYCODE_STEM_PRIMARY");
        BUTTON_NAMES.put(265, "KEYCODE_STEM_1");
        BUTTON_NAMES.put(266, "KEYCODE_STEM_2");
        BUTTON_NAMES.put(267, "KEYCODE_STEM_3");
        BUTTON_NAMES.put(268, "KEYCODE_DPAD_UP_LEFT");
        BUTTON_NAMES.put(269, "KEYCODE_DPAD_DOWN_LEFT");
        BUTTON_NAMES.put(270, "KEYCODE_DPAD_UP_RIGHT");
        BUTTON_NAMES.put(271, "KEYCODE_DPAD_DOWN_RIGHT");
        BUTTON_NAMES.put(272, "KEYCODE_MEDIA_SKIP_FORWARD");
        BUTTON_NAMES.put(273, "KEYCODE_MEDIA_SKIP_BACKWARD");
        BUTTON_NAMES.put(274, "KEYCODE_MEDIA_STEP_FORWARD");
        BUTTON_NAMES.put(275, "KEYCODE_MEDIA_STEP_BACKWARD");
        BUTTON_NAMES.put(276, "KEYCODE_SOFT_SLEEP");
        BUTTON_NAMES.put(277, "KEYCODE_CUT");
        BUTTON_NAMES.put(278, "KEYCODE_COPY");
        BUTTON_NAMES.put(279, "KEYCODE_PASTE");
        BUTTON_NAMES.put(280, "KEYCODE_SYSTEM_NAVIGATION_UP");
        BUTTON_NAMES.put(281, "KEYCODE_SYSTEM_NAVIGATION_DOWN");
        BUTTON_NAMES.put(282, "KEYCODE_SYSTEM_NAVIGATION_LEFT");
        BUTTON_NAMES.put(283, "KEYCODE_SYSTEM_NAVIGATION_RIGHT");
        BUTTON_NAMES.put(284, "KEYCODE_ALL_APPS");
        BUTTON_NAMES.put(285, "KEYCODE_REFRESH");
    }

    public static Map<Integer, String> AXIS_NAMES = new HashMap<>();

    static {
        AXIS_NAMES.put(AXIS_X, "AXIS_X");
        AXIS_NAMES.put(AXIS_Y, "AXIS_Y");
        AXIS_NAMES.put(AXIS_PRESSURE, "AXIS_PRESSURE");
        AXIS_NAMES.put(AXIS_SIZE, "AXIS_SIZE");
        AXIS_NAMES.put(AXIS_TOUCH_MAJOR, "AXIS_TOUCH_MAJOR");
        AXIS_NAMES.put(AXIS_TOUCH_MINOR, "AXIS_TOUCH_MINOR");
        AXIS_NAMES.put(AXIS_TOOL_MAJOR, "AXIS_TOOL_MAJOR");
        AXIS_NAMES.put(AXIS_TOOL_MINOR, "AXIS_TOOL_MINOR");
        AXIS_NAMES.put(AXIS_ORIENTATION, "AXIS_ORIENTATION");
        AXIS_NAMES.put(AXIS_VSCROLL, "AXIS_VSCROLL");
        AXIS_NAMES.put(AXIS_HSCROLL, "AXIS_HSCROLL");
        AXIS_NAMES.put(AXIS_Z, "AXIS_Z");
        AXIS_NAMES.put(AXIS_RX, "AXIS_RX");
        AXIS_NAMES.put(AXIS_RY, "AXIS_RY");
        AXIS_NAMES.put(AXIS_RZ, "AXIS_RZ");
        AXIS_NAMES.put(AXIS_HAT_X, "AXIS_HAT_X");
        AXIS_NAMES.put(AXIS_HAT_Y, "AXIS_HAT_Y");
        AXIS_NAMES.put(AXIS_LTRIGGER, "AXIS_LTRIGGER");
        AXIS_NAMES.put(AXIS_RTRIGGER, "AXIS_RTRIGGER");
        AXIS_NAMES.put(AXIS_THROTTLE, "AXIS_THROTTLE");
        AXIS_NAMES.put(AXIS_RUDDER, "AXIS_RUDDER");
        AXIS_NAMES.put(AXIS_WHEEL, "AXIS_WHEEL");
        AXIS_NAMES.put(AXIS_GAS, "AXIS_GAS");
        AXIS_NAMES.put(AXIS_BRAKE, "AXIS_BRAKE");
        AXIS_NAMES.put(AXIS_DISTANCE, "AXIS_DISTANCE");
        AXIS_NAMES.put(AXIS_TILT, "AXIS_TILT");
        AXIS_NAMES.put(AXIS_SCROLL, "AXIS_SCROLL");
        AXIS_NAMES.put(AXIS_RELATIVE_X, "AXIS_REALTIVE_X");
        AXIS_NAMES.put(AXIS_RELATIVE_Y, "AXIS_REALTIVE_Y");
        AXIS_NAMES.put(AXIS_GENERIC_1, "AXIS_GENERIC_1");
        AXIS_NAMES.put(AXIS_GENERIC_2, "AXIS_GENERIC_2");
        AXIS_NAMES.put(AXIS_GENERIC_3, "AXIS_GENERIC_3");
        AXIS_NAMES.put(AXIS_GENERIC_4, "AXIS_GENERIC_4");
        AXIS_NAMES.put(AXIS_GENERIC_5, "AXIS_GENERIC_5");
        AXIS_NAMES.put(AXIS_GENERIC_6, "AXIS_GENERIC_6");
        AXIS_NAMES.put(AXIS_GENERIC_7, "AXIS_GENERIC_7");
        AXIS_NAMES.put(AXIS_GENERIC_8, "AXIS_GENERIC_8");
        AXIS_NAMES.put(AXIS_GENERIC_9, "AXIS_GENERIC_9");
        AXIS_NAMES.put(AXIS_GENERIC_10, "AXIS_GENERIC_10");
        AXIS_NAMES.put(AXIS_GENERIC_11, "AXIS_GENERIC_11");
        AXIS_NAMES.put(AXIS_GENERIC_12, "AXIS_GENERIC_12");
        AXIS_NAMES.put(AXIS_GENERIC_13, "AXIS_GENERIC_13");
        AXIS_NAMES.put(AXIS_GENERIC_14, "AXIS_GENERIC_14");
        AXIS_NAMES.put(AXIS_GENERIC_15, "AXIS_GENERIC_15");
        AXIS_NAMES.put(AXIS_GENERIC_16, "AXIS_GENERIC_16");
    }

    public static final List<ControllerAction> CONTROLLER_ACTIONS = new ArrayList<>();

    static {
        CONTROLLER_ACTIONS.add(new ControllerAction(LEFT, KEYCODE_DPAD_LEFT));
        CONTROLLER_ACTIONS.add(new ControllerAction(RIGHT, KEYCODE_DPAD_RIGHT));
        CONTROLLER_ACTIONS.add(new ControllerAction(UP, KEYCODE_DPAD_UP));
        CONTROLLER_ACTIONS.add(new ControllerAction(DOWN, KEYCODE_DPAD_DOWN));
        CONTROLLER_ACTIONS.add(new ControllerAction(B, KEYCODE_BUTTON_A));
        CONTROLLER_ACTIONS.add(new ControllerAction(A, KEYCODE_BUTTON_B));
        CONTROLLER_ACTIONS.add(new ControllerAction(Y, KEYCODE_BUTTON_X));
        CONTROLLER_ACTIONS.add(new ControllerAction(X, KEYCODE_BUTTON_Y));
        CONTROLLER_ACTIONS.add(new ControllerAction(R, KEYCODE_BUTTON_R1));
        CONTROLLER_ACTIONS.add(new ControllerAction(ZR, KEYCODE_BUTTON_R2));
        CONTROLLER_ACTIONS.add(new ControllerAction(RIGHT_SR, KEYCODE_BUTTON_1));
        CONTROLLER_ACTIONS.add(new ControllerAction(LEFT_SR, KEYCODE_BUTTON_2));
        CONTROLLER_ACTIONS.add(new ControllerAction(L, KEYCODE_BUTTON_L1));
        CONTROLLER_ACTIONS.add(new ControllerAction(ZL, KEYCODE_BUTTON_L2));
        CONTROLLER_ACTIONS.add(new ControllerAction(RIGHT_SL, KEYCODE_BUTTON_3));
        CONTROLLER_ACTIONS.add(new ControllerAction(LEFT_SL, KEYCODE_BUTTON_4));
        CONTROLLER_ACTIONS.add(new ControllerAction(PLUS, KEYCODE_BUTTON_START));
        CONTROLLER_ACTIONS.add(new ControllerAction(MINUS, KEYCODE_BUTTON_SELECT));
        CONTROLLER_ACTIONS.add(new ControllerAction(HOME, KEYCODE_BUTTON_MODE));
        CONTROLLER_ACTIONS.add(new ControllerAction(CAPTURE, KEYCODE_BUTTON_Z));
        CONTROLLER_ACTIONS.add(new ControllerAction(LEFT_STICK, KEYCODE_BUTTON_THUMBL));
        CONTROLLER_ACTIONS.add(new ControllerAction(RIGHT_STICK, KEYCODE_BUTTON_THUMBR));
        CONTROLLER_ACTIONS.add(new ControllerAction(RIGHT_JOYSTICK, AXIS_Z, 1, AXIS_RZ, -1));
        CONTROLLER_ACTIONS.add(new ControllerAction(LEFT_JOYSTICK, AXIS_X, 1, AXIS_Y, -1));
    }


    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Map<Integer, ButtonType> getButtonMapping(Context context) {
        return getButtonMapping(getControllerActions(context));
    }

    public static Map<Integer, ButtonType> getButtonMapping(List<ControllerAction> controllerActions) {
        return controllerActions
                .stream()
                .filter(ca -> ca.getType() == BUTTON)
                .collect(Collectors.toMap(ControllerAction::getKey, ControllerAction::getButton));
    }

    public static Map<Pair<Integer, Integer>, ButtonType> getAxisMapping(Context context) {
        return getAxisMapping(getControllerActions(context));
    }

    public static Map<Pair<Integer, Integer>, ButtonType> getAxisMapping(List<ControllerAction> controllerActions) {
        return controllerActions
                .stream()
                .filter(ca -> ca.getType() == AXIS)
                .map(ca -> new Pair<>(new Pair<>(ca.getXAxis(), ca.getXDirection()), ca.getButton()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    public static Map<JoystickType, ControllerAction> getJoystickMapping(Context context) {
        return getJoystickMapping(getControllerActions(context));
    }

    public static Map<JoystickType, ControllerAction> getJoystickMapping(List<ControllerAction> controllerActions) {
        return controllerActions
                .stream()
                .filter(ca -> ca.getType() == JOYSTICK)
                .collect(Collectors.toMap(ControllerAction::getJoystick, Function.identity()));
    }

    public static List<ControllerAction> getControllerActions(Context context) {
        return Optional.ofNullable(PreferenceUtils.getButtonMapping(context))
                .map(objStr -> {
                    try {
                        return OBJECT_MAPPER.<List<ControllerAction>>readValue(objStr, new TypeReference<List<ControllerAction>>() {
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(CONTROLLER_ACTIONS);
    }

    public static void setControllerActions(Context context, List<ControllerAction> actions) {
        try {
            String actionsStr = OBJECT_MAPPER.writeValueAsString(actions);
            PreferenceUtils.setButtonMapping(context, actionsStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
