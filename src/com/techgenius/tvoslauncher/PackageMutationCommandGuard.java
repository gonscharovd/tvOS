package com.techgenius.tvoslauncher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classifies package-state shell commands before they reach the ADB transport.
 * Only a single user-0 disable command is eligible for the shared safety policy.
 */
public final class PackageMutationCommandGuard {
    public enum Type { NONE, DISABLE, ENABLE, DELETE, UNSUPPORTED_MUTATION }

    public static final class Result {
        public final Type type;
        public final String packageName;

        private Result(Type type, String packageName) {
            this.type = type;
            this.packageName = packageName;
        }
    }

    private static final Pattern DISABLE_COMMAND = Pattern.compile(
            "(?i)^\\s*(?:pm\\s+(?:disable-user|disable)|cmd\\s+package\\s+(?:disable-user|disable))"
                    + "\\s+--user\\s+0\\s+([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+)\\s*$");
    private static final Pattern ENABLE_COMMAND = Pattern.compile(
            "(?i)^\\s*(?:pm\\s+enable|cmd\\s+package\\s+enable)"
                    + "\\s+--user\\s+0\\s+([A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+)\\s*$");
    private static final Pattern MUTATION_TOKEN = Pattern.compile(
            "(?i)\\b(?:pm\\s+(?:disable(?:-user|-until-used)?|enable|uninstall(?:-user)?|uninstall-system-updates|suspend(?:-user)?|hide|set-enabled-setting|set-hidden-setting)"
                    + "|cmd\\s+package\\s+(?:disable(?:-user|-until-used)?|enable|uninstall|suspend|hide|set-enabled-setting|set-hidden-setting))\\b");
    private static final Pattern DELETE_TOKEN = Pattern.compile(
            "(?i)\\b(?:pm\\s+(?:uninstall(?:-user)?|uninstall-system-updates)"
                    + "|cmd\\s+package\\s+uninstall)\\b");

    private PackageMutationCommandGuard() { }

    public static Result inspect(String command) {
        if (command == null || !MUTATION_TOKEN.matcher(command).find()) {
            return new Result(Type.NONE, null);
        }
        if (DELETE_TOKEN.matcher(command).find()) {
            return new Result(Type.DELETE, null);
        }
        Matcher matcher = DISABLE_COMMAND.matcher(command);
        if (matcher.matches()) {
            String packageName = matcher.group(1);
            return PackageSafetyPolicy.isValidPackageName(packageName)
                    ? new Result(Type.DISABLE, packageName)
                    : new Result(Type.UNSUPPORTED_MUTATION, null);
        }
        matcher = ENABLE_COMMAND.matcher(command);
        if (matcher.matches()) {
            String packageName = matcher.group(1);
            return PackageSafetyPolicy.isValidPackageName(packageName)
                    ? new Result(Type.ENABLE, packageName)
                    : new Result(Type.UNSUPPORTED_MUTATION, null);
        }
        return new Result(Type.UNSUPPORTED_MUTATION, null);
    }
}
