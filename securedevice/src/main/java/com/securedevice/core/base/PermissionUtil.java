package com.securedevice.core.base;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.core.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    private String permission;
    private static String PERMISSION_DELIMITER = "\\|";
    private static int AND = '&';
    private static int OR = '|';
    private static int OPEN_EXPR = '(';
    private static int CLOSE_EXPR = ')';
    private Context context;

    public PermissionUtil(String permission, Context context) {
        this.permission = permission;
        this.context = context;
    }

    public static class PermissionOperatorBuilder {
        private PermissionRequestBuilder requestBuilder;

        public PermissionOperatorBuilder(PermissionRequestBuilder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        public PermissionRequestBuilder and() {
            requestBuilder.permissions = requestBuilder.permissions.concat(" ").concat(PermissionType.ALL_MANDATORY.getExt()).concat(" ");
            return requestBuilder;
        }

        public PermissionRequestBuilder or() {
            requestBuilder.permissions = requestBuilder.permissions.concat(" ").concat(PermissionType.AT_LEAST_ONCE.getExt()).concat(" ");
            return requestBuilder;
        }

        public PermissionUtil build() {
            return new PermissionUtil(requestBuilder.permissions, requestBuilder.context);
        }
    }

    public static class PermissionRequestBuilder {
        private Context context;
        private String libPath;
        private String permissions;

        /**
         * @param context
         * @ use {@link PermissionRequestBuilder(Context, String)}
         */
        public PermissionRequestBuilder(Context context) {
            this.context = context;
        }


        PermissionOperatorBuilder handlePermissions(PermissionType type, String[] newPermissions) {
            if (newPermissions == null || newPermissions.length == 0)
                throw new IllegalArgumentException("At least a permission must be specified");
            if (permissions == null)
                permissions = "";
            StringBuilder builder = new StringBuilder();
            builder.append("(").append(" ");
            String add;
            if (newPermissions.length == 1) {
                builder.append(newPermissions[0]);
                add = builder.toString();
            } else {
                for (String current : newPermissions) {
                    builder.append(current).append(" ").append(type.getExt()).append(" ");
                }
                add = builder.substring(0, builder.length() - 3);
            }
            add = add.concat(" ").concat(")");
            permissions = permissions.concat(add);
            return new PermissionOperatorBuilder(this);
        }

        /**
         * Add a block of permissions. At least once must be granted to pass this check
         *
         * @param type           one of {@link PermissionType}
         * @param newPermissions list of permission
         * @return PermissionOperatorBuilder
         */
        public PermissionOperatorBuilder addPermissions(PermissionType type, String... newPermissions) {
            return this.handlePermissions(type, newPermissions);
        }


        public PermissionOperatorBuilder addPermission(String... newPermissions) {
            return this.handlePermissions(PermissionType.ALL_MANDATORY, newPermissions);
        }

        public PermissionUtil addStringPermissions(String newPermissions) {
            this.permissions = newPermissions;
            return build();
        }


        public PermissionUtil build() {
            return new PermissionUtil(permissions, context);
        }
    }

    /**
     * Object used internally to handle each block permission result
     */
    public static class PermissionContext {
        private List<String> permissions;
        private boolean result;

        public boolean isSuccess() {
            return result;
        }

        void setResult(boolean result) {
            this.result = result;
        }

        public List<String> getMissingPermissions() {
            return permissions;
        }

        void addPermission(String permission) {
            if (permissions == null)
                permissions = new ArrayList<>();
            permissions.add(permission);
        }

        void addPermissions(List<String> permission) {
            if (permissions == null)
                permissions = new ArrayList<>();
            if (permission == null || permission.isEmpty())
                return;
            permissions.addAll(permission);
        }
    }

    private static PermissionContext eval(final String str, final Context context) {
        return new Object() {
            int pos = -1;
            int ch;


            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ')
                    nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            PermissionContext parse() {
                nextChar();
                PermissionContext x = parseExpression();
                if (pos < str.length())
                    throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            PermissionContext parseExpression() {
                PermissionContext x = checkPermission();
                int startPos = this.pos;
                while (eat(AND) || eat(OR)) {
                    PermissionContext internalPermissionCheck = checkPermission();
                    String sep = str.substring(startPos).trim().substring(0, 1);
                    if (sep.equalsIgnoreCase("&")) {
                        x.setResult(x.isSuccess() && internalPermissionCheck.isSuccess());
                    } else if (sep.equalsIgnoreCase("|")) {
                        x.setResult(x.isSuccess() || internalPermissionCheck.isSuccess());
                    }
                    x.addPermissions(internalPermissionCheck.permissions);
                }
                return x;
            }

            @SuppressLint("WrongConstant")
            PermissionContext checkPermission() {
                PermissionContext permissionContext = new PermissionContext();
                int startPos = this.pos;
                if (eat(OPEN_EXPR)) { // parentheses
                    permissionContext = parseExpression();
                    eat(CLOSE_EXPR);
                } else if ((this.ch >= 'a' && this.ch <= 'z') || (this.ch >= 'A' && this.ch <= 'Z') || this.ch == '.' || this.ch == ' ' || this.ch == '_') { // permission
                    while ((this.ch >= 'a' && this.ch <= 'z') || (this.ch >= 'A' && this.ch <= 'Z') || this.ch == '.' || this.ch == ' ' || this.ch == '_')
                        nextChar();
                    String permission = str.substring(startPos, this.pos).trim();
                    if (PermissionChecker.checkSelfPermission(context, permission) == 0)
                        permissionContext.setResult(true);
                    else
                        permissionContext.addPermission(permission);
                    // SU
                } else {
                    throw new RuntimeException("Unexpected: " + (char) this.ch);
                }

                return permissionContext;
            }
        }.parse();

    }

    public static PermissionContext checkPermission(PermissionUtil instance) {
        String permissionExpression = instance.getPermission();
        Context context = instance.getContext();
        return eval(permissionExpression, context);
    }


    private String getPermission() {
        return permission;
    }

    private Context getContext() {
        return context;
    }

}
