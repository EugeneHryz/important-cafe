package com.eugene.cafe.controller.command;

public enum CommandType {
    LOG_IN,
    LOG_OUT,
    DEFAULT,
    GO_TO_SIGNUP_PAGE,
    GO_TO_LOGIN_PAGE,
    SIGN_UP,
    CHANGE_LOCALE,
    GO_TO_PROFILE_PAGE,
    EDIT_USER_PROFILE,
    UPDATE_PROFILE_PICTURE,
    CHANGE_PASSWORD,
    GO_TO_ADMIN_DASHBOARD_PAGE,
    ADD_MENU_ITEM,
    GO_TO_MENU_PAGE,
    CHANGE_SORT_ORDER,
    CHANGE_CURRENT_CATEGORY,
    TOP_UP_BALANCE,
    GO_TO_CHECKOUT_PAGE,
    PLACE_ORDER,
    GO_TO_ORDER_HISTORY_PAGE,
    SAVE_REVIEW,
    GO_TO_ORDER_PAGE,
    CHANGE_ORDER_STATUS,

    GET_USER_COUNT,
    LOAD_USER_PAGE,
    BAN_USER,
    UNBAN_USER,
    ADD_ITEM_TO_CART,
    REMOVE_ITEM_FROM_CART,
    GET_MENU_ITEM_COUNT,
    LOAD_MENU_PAGE,
    DELETE_ITEM,
}
