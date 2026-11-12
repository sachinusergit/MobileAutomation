package com.enterprise.mobile.driver;

import com.enterprise.mobile.config.ConfigManager;
import com.enterprise.mobile.config.FrameworkConstants;
import com.enterprise.mobile.exceptions.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Supplies a distinct device to each parallel test thread. Devices are taken
 * from a shared pool and returned once the driver has been quit.
 *
 * <p>When running in parallel there must be at least as many registered
 * devices as TestNG threads.</p>
 */
public final class DeviceAllocator {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceAllocator.class);
    private static final ConcurrentLinkedDeque<Device> AVAILABLE = new ConcurrentLinkedDeque<>();
    private static final AtomicBoolean INITIALISED = new AtomicBoolean(false);

    private DeviceAllocator() {
    }

    public static Device acquire() {
        initialise();
        Device device = AVAILABLE.poll();
        if (device == null) {
            throw new FrameworkException("""
                    No device available for the current thread. Register additional devices in \
                    src/test/resources/devices/devices.json or reduce the TestNG parallel thread count.""");
        }
        LOG.info("Device allocated to thread '{}': {}", Thread.currentThread().getName(), device.displayName());
        return device;
    }

    public static void release(Device device) {
        if (device != null) {
            // Return to the head so sequential suites keep using the same device.
            AVAILABLE.offerFirst(device);
            LOG.info("Device released by thread '{}': {}", Thread.currentThread().getName(), device.displayName());
        }
    }

    public static int availableCount() {
        initialise();
        return AVAILABLE.size();
    }

    public static synchronized void reset() {
        AVAILABLE.clear();
        INITIALISED.set(false);
    }

    private static void initialise() {
        if (INITIALISED.compareAndSet(false, true)) {
            Platform platform = Platform.current();
            List<Device> devices = DeviceRepository.load(platform);
            AVAILABLE.addAll(devices);
            int expectedThreads = ConfigManager.getInt("parallel.threads", devices.size());
            if (expectedThreads > devices.size()) {
                LOG.warn("Parallel thread count ({}) exceeds registered device count ({}); "
                        + "extra threads will fail to acquire a device", expectedThreads, devices.size());
            }
        }
    }
}
