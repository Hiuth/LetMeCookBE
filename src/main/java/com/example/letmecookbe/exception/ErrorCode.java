package com.example.letmecookbe.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {

    EMAIL_EXISTED(1001, "Email already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "User not exists", HttpStatus.NOT_FOUND),
    OTP_INCORRECT(1003, "OTP incorrect", HttpStatus.BAD_REQUEST),
    NAME_INVALID(1004, "Name must be at least 5 characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1005, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1006, "Invalid email", HttpStatus.BAD_REQUEST),
    BIRTHDAY_INVALID(1007, "Birthday must be a day in the past", HttpStatus.BAD_REQUEST),
    GENDER_INVALID(1008, "Gender must be either 'male' or 'female'", HttpStatus.BAD_REQUEST),
    ACCOUNT_EMPTY(1009, "List of accounts is empty", HttpStatus.NO_CONTENT),
    NOT_NULL(1010, "Something is blank", HttpStatus.BAD_REQUEST),
    PERSONAL_INFORMATION_EMPTY(1011, "List of accounts is empty", HttpStatus.NO_CONTENT),
    PERINFO_EMPTY(1012, "User has not added personal information", HttpStatus.NOT_FOUND),
    ACCOUNT_BANNED(1013, "Account has been banned", HttpStatus.FORBIDDEN),
    PASSWORD_NOT_CORRECT(1014, "Password not correct", HttpStatus.UNAUTHORIZED),
    SEND_EMAIL_FAILED(1015, "Send email failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_INVALID(1016, "Folder and name must be at least 3 characters", HttpStatus.BAD_REQUEST),
    CATEGORY_EXISTED(1017, "This main category already exists", HttpStatus.CONFLICT),
    _CATEGORY_EXISTED(1017, "SUBThis main category already exists", HttpStatus.CONFLICT),
    SUBCATEGORY_BLANK(1018, "Sub-category must not be blank", HttpStatus.BAD_REQUEST),
    LIST_EMPTY(1019, "List is empty", HttpStatus.NO_CONTENT),
    SUB_CATEGORY_EXISTED(1001, "This Sub-category already exists", HttpStatus.BAD_REQUEST),
    MAIN_CATEGORY_NOT_EXIST(1020, "This main does not exist", HttpStatus.NOT_FOUND),
    SUB_CATEGORY_NOT_EXIST(1020, "This sub does not exist", HttpStatus.NOT_FOUND),
    NOTIFICATION_LENGTH(1021, "Title and content must be at least 5 characters", HttpStatus.BAD_REQUEST),
    NOTIFICATION_EXISTED(1022, "Notification already exists", HttpStatus.CONFLICT),
    NOTIFICATION_NOT_EXISTED(1023, "Notification not exists", HttpStatus.NOT_FOUND),
    FEEDBACK_EXISTED(1024, "You have already sent feedback. If you want to change anything, please update the old feedback.", HttpStatus.CONFLICT),
    RECIPE_EMPTY(1025, "Recipe is empty", HttpStatus.BAD_REQUEST),
    RECIPE_NAME_INVALID(1026, "File name must be at least 3 characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1027, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    DOC_NOT_EXIST(1028, "Document does not exist", HttpStatus.NOT_FOUND),
    COMMENT_NOT_EXIST(1029, "Comment does not exist", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_POINT_TO_DOWNLOAD(1030, "Not enough points to download", HttpStatus.FORBIDDEN),
    INCORRECT_OTP(1031, "Incorrect OTP", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1032, "You do not have permission", HttpStatus.FORBIDDEN),
    USERNAME_EXISTED(1033, "Username already exists", HttpStatus.BAD_REQUEST),
    FAILED_TO_SENT_EMAIL(1034, "Failed to sent email", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_HASHING(1035,"Error hashing input when hash a otp", HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_EXPIRED(1036, "OTP expired", HttpStatus.UNAUTHORIZED),
    FEEDBACK_NOT_FOUND(1037, "Feedback does not exist", HttpStatus.NOT_FOUND),
    PER_INFO_EXISTED(1038, "Per-info already exists", HttpStatus.CONFLICT),
    DRIVE_UPLOAD_FAILED(1039, "Drive upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FEEDBACK_TYPE_INCORRECT(1040,"Incorrect feedback type", HttpStatus.BAD_REQUEST),
    NOTIFICATION_INVALID(1041,"Notification invalid", HttpStatus.BAD_REQUEST),
    INGREDIENT_NOT_FOUND(1042,"Ingredient not found", HttpStatus.NOT_FOUND),
    INGREDIENT_EXISTED(1043,"Ingredient already exists", HttpStatus.CONFLICT),
    INGREDIENT_NOT_EXISTED(1044,"Ingredient not found", HttpStatus.NOT_FOUND),
    FILE_NOT_FOUND(1055, "File not found", HttpStatus.NOT_FOUND),
    FILE_READ_FAILED(1056, "Failed to read file", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_INFO_NOT_EXISTED(1054, "User info not exists", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND(1050, "Account not found", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTED(1051, "Email already existed", HttpStatus.BAD_REQUEST),
    ACCOUNT_NOT_EXISTED(1052, "Account not existed", HttpStatus.NOT_FOUND),
    VERIFICATION_CODE_INVALID(1053, "Verification code invalid", HttpStatus.BAD_REQUEST),
    INVALID_BAN_DURATION(1054, "Invalid ban duration", HttpStatus.BAD_REQUEST),
    USER_INFO_NOT_FOUND(1055, "User info not found", HttpStatus.NOT_FOUND),
    FILE_EMPTY(1056, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1057, "File too large", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_TYPE(1058, "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1059, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FILE_FAILED(1060, "Delete file failed", HttpStatus.INTERNAL_SERVER_ERROR),
    RECIPE_NOT_FOUND(1045,"Recipe not found", HttpStatus.NOT_FOUND),
    RECIPE_INGREDIENTS_NOT_EXISTED(1046,"Recipe ingredients not found", HttpStatus.NOT_FOUND),
    RECIPE_INGREDIENTS_EXISTED(1047,"Recipe ingredients already exists", HttpStatus.CONFLICT),
    RECIPE_STEPS_NOT_EXISTED(1048,"Recipe steps not found", HttpStatus.NOT_FOUND),
    RECIPE_STEPS_EXISTED(1049,"Recipe steps already exists", HttpStatus.CONFLICT),
    FAVOURITE_RECIPE_NOT_EXISTED(1050,"Favourite recipe not found", HttpStatus.NOT_FOUND),
    FAVOURITE_RECIPE_EXISTED(1051,"Favourite recipe already exists", HttpStatus.CONFLICT),
    LIKE_RECIPE_EXISTED(1052,"Like recipe already exists", HttpStatus.CONFLICT),
    LIKE_RECIPE_NOT_EXISTED(1053,"Like recipe not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1054,"Comment not found", HttpStatus.NOT_FOUND),
    LIKE_COMMENT_EXISTED(1055,"Like comment already exists", HttpStatus.CONFLICT),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_GOOGLE_TOKEN(8887, "Invalid Google token", HttpStatus.UNAUTHORIZED),
    INVALID_INPUT(8886, "Invalid input", HttpStatus.BAD_REQUEST),
    INVALID_DIET_TYPE(8885, "Invalid diet type", HttpStatus.BAD_REQUEST),
    REPORT_NOT_FOUND(8884, "Report not found", HttpStatus.NOT_FOUND),
    CANNOT_REPORT_OWN_CONTENT(8883, "Cannot report own content", HttpStatus.BAD_REQUEST),
    CANNOT_REPORT_OWN_ACCOUNT(8884, "Cannot report own account", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(8884, "User not found", HttpStatus.NOT_FOUND),
    INVALID_KEY(8888, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_COMMENT_STATUS(8889, "Invalid comment status", HttpStatus.BAD_REQUEST),
    getEvidenceImage(8889, "Evidence image", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR(8889, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_RECIPE_STATUS(8889, "Invalid recipe status", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}
