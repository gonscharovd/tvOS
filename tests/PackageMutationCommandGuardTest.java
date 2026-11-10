package com.techgenius.tvoslauncher;

public final class PackageMutationCommandGuardTest {
    private static void expect(PackageMutationCommandGuard.Type type, String pkg, String command) {
        PackageMutationCommandGuard.Result result = PackageMutationCommandGuard.inspect(command);
        if (result.type != type || (pkg == null ? result.packageName != null : !pkg.equals(result.packageName))) {
            throw new AssertionError("Unexpected classification for [" + command + "]: "
                    + result.type + "/" + result.packageName);
        }
    }

    public static void main(String[] args) {
        expect(PackageMutationCommandGuard.Type.NONE, null, "settings put global private_dns_mode off");
        expect(PackageMutationCommandGuard.Type.DISABLE, "com.example.app",
                "pm disable-user --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.DISABLE, "com.example.app",
                "pm disable --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.DISABLE, "com.example.app",
                "cmd package disable-user --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.ENABLE, "com.example.app",
                "pm enable --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.ENABLE, "com.example.app",
                "cmd package enable --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm enable --user 0 com.example.app; echo done");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm enable --user 0 com.example.app; settings put global adb_enabled 0");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm enable --user 0 com.example.app; pm disable-user --user 0 com.other.app");
        expect(PackageMutationCommandGuard.Type.DELETE, null,
                "pm uninstall --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.DELETE, null,
                "cmd package uninstall --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.DELETE, null, "pm uninstall-system-updates");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "echo ok; pm disable-user --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm disable-user --user 10 com.example.app");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm suspend --user 0 com.example.app");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "cmd package set-enabled-setting com.example.app disabled-user");
        expect(PackageMutationCommandGuard.Type.UNSUPPORTED_MUTATION, null,
                "pm disable-user --user 0 com.example.app; echo done");
        expect(PackageMutationCommandGuard.Type.DELETE, null,
                "pm disable-user --user 0 com.example.app; pm uninstall --user 0 com.other.app");
        System.out.println("Package mutation command guard tests passed.");
    }
}
