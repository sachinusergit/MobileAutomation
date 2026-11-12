package com.enterprise.mobile.utils;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Minimal, guarded wrapper around the Android Debug Bridge. ADB is optional:
 * methods degrade gracefully when the executable cannot be located.
 */
public final class AdbUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AdbUtils.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    private AdbUtils() {
    }

    public static boolean isAvailable() {
        return locateAdb() != null;
    }

    public static List<String> connectedDevices() {
        List<String> devices = new ArrayList<>();
        String output = execute(List.of("devices"));
        for (String line : output.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("List of devices")) {
                continue;
            }
            String[] parts = trimmed.split("\\s+");
            if (parts.length >= 2 && !"offline".equalsIgnoreCase(parts[1])) {
                devices.add(parts[0]);
            }
        }
        return devices;
    }

    public static boolean isDeviceOnline(String udid) {
        return connectedDevices().stream().anyMatch(device -> device.equalsIgnoreCase(udid));
    }

    /**
     * Waits until the device answers again. A wireless device can go offline
     * for a moment (screen off, Wi-Fi hiccup); reconnecting brings it back.
     *
     * @return {@code true} when the device is online
     */
    public static boolean waitForDeviceOnline(String udid, int seconds) {
        if (udid == null || udid.isBlank()) {
            return true;
        }
        for (int attempt = 0; attempt < seconds; attempt++) {
            if (isDeviceOnline(udid)) {
                return true;
            }
            if (udid.matches(".*:\\d+")) {
                try {
                    connect(udid);
                } catch (RuntimeException e) {
                    LOG.warn("Could not reconnect {}: {}", udid, e.getMessage());
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isDeviceOnline(udid);
    }

    /** Keeps the screen on so a wireless device does not drop off. */
    public static void keepScreenOn(String udid) {
        try {
            shell(udid, "svc power stayon true");
            shell(udid, "settings put system screen_off_timeout 1800000");
        } catch (RuntimeException e) {
            LOG.warn("Could not keep the screen on: {}", e.getMessage());
        }
    }

    /**
     * Reconnects a wireless device ("ip:port"). The wireless debugging port of
     * the phone changes on every reconnect, so the framework re-establishes the
     * connection before a session is created.
     *
     * @return {@code true} when the device answered
     */
    public static boolean connect(String hostAndPort) {
        if (hostAndPort == null || !hostAndPort.matches(".*:\\d+")) {
            return false;
        }
        String output = execute(List.of("connect", hostAndPort));
        boolean connected = output.toLowerCase().contains("connected");
        if (!connected) {
            LOG.warn("Could not connect to {}: {}", hostAndPort, output.trim());
        }
        return connected;
    }

    public static String shell(String udid, String command) {
        return execute(List.of("-s", udid, "shell", command));
    }

    public static void install(String udid, String apkPath) {
        Path apk = Paths.get(apkPath).toAbsolutePath();
        if (!Files.exists(apk)) {
            throw new FrameworkException("APK not found: " + apk);
        }
        execute(List.of("-s", udid, "install", "-r", apk.toString()));
    }

    public static void startActivity(String udid, String appPackage, String appActivity) {
        shell(udid, "am start -n %s/%s".formatted(appPackage, appActivity));
    }

    public static void grantPermission(String udid, String appPackage, String permission) {
        shell(udid, "pm grant %s %s".formatted(appPackage, permission));
    }

    public static void setAnimationScale(String udid, String scale) {
        shell(udid, "settings put global window_animation_scale " + scale);
        shell(udid, "settings put global transition_animation_scale " + scale);
        shell(udid, "settings put global animator_duration_scale " + scale);
    }

    public static String execute(List<String> arguments) {
        String adb = locateAdb();
        if (adb == null) {
            throw new FrameworkException("ADB executable was not found. Configure 'adb.path' or set ANDROID_SDK_ROOT.");
        }
        List<String> command = new ArrayList<>();
        command.add(adb);
        command.addAll(arguments);

        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            String output;
            try (InputStream stream = process.getInputStream()) {
                output = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new FrameworkException("ADB command timed out: " + String.join(" ", command));
            }
            if (process.exitValue() != 0) {
                LOG.warn("ADB command exited with {}: {}", process.exitValue(), output.trim());
            }
            return output;
        } catch (IOException e) {
            throw new FrameworkException("Unable to execute ADB command: " + String.join(" ", command), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FrameworkException("ADB execution interrupted", e);
        }
    }

    private static String locateAdb() {
        String configured = ConfigManager.get(FrameworkConstants.ADB_PATH);
        if (configured != null && !configured.isBlank()) {
            Path path = Paths.get(configured);
            if (Files.isRegularFile(path)) {
                return path.toAbsolutePath().toString();
            }
        }
        String executable = isWindows() ? "adb.exe" : "adb";
        for (String sdkVariable : List.of("ANDROID_SDK_ROOT", "ANDROID_HOME")) {
            String sdkRoot = System.getenv(sdkVariable);
            if (sdkRoot != null && !sdkRoot.isBlank()) {
                Path candidate = Paths.get(sdkRoot, "platform-tools", executable);
                if (Files.isRegularFile(candidate)) {
                    return candidate.toAbsolutePath().toString();
                }
            }
        }
        return null;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
