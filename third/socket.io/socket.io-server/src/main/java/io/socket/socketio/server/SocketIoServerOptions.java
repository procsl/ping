package io.socket.socketio.server;

/**
 * Options for {@link SocketIoServer}
 */
@SuppressWarnings("WeakerAccess")
public final class SocketIoServerOptions {

    /**
     * The default options used by server.
     * This instance is locked and cannot be modified.
     *
     * adapter factory: {@link SocketIoMemoryAdapter.Factory}
     */
    public static final SocketIoServerOptions DEFAULT = new SocketIoServerOptions();

    private static final SocketIoAdapter.AdapterFactory MEMORY_ADAPTER_FACTORY = new SocketIoMemoryAdapter.Factory();

    static {
        DEFAULT.setAdapterFactory(MEMORY_ADAPTER_FACTORY);
        DEFAULT.setConnectionTimeout(45000);
        DEFAULT.lock();
    }

    private boolean mIsLocked;
    private long mConnectionTimeout;
    private SocketIoAdapter.AdapterFactory mAdapterFactory;

    private SocketIoServerOptions() {
        mIsLocked = false;
    }

    /**
     * Create a new instance of {@link SocketIoServerOptions} by copying
     * default options.
     *
     * @return New instance of {@link SocketIoServerOptions} with default options.
     */
    public static SocketIoServerOptions newFromDefault() {
        return (new SocketIoServerOptions())
                .setConnectionTimeout(DEFAULT.getConnectionTimeout())
                .setAdapterFactory(DEFAULT.getAdapterFactory());
    }

    /**
     * Gets the amount of time to wait (ms) before a client without namespace is closed.
     */
    public long getConnectionTimeout() {
        return mConnectionTimeout;
    }

    /**
     * Sets the amount of time to wait (ms) before a client without namespace is closed.
     */
    public SocketIoServerOptions setConnectionTimeout(long connectionTimeout) {
        mConnectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Gets the adapter factory instance.
     */
    public SocketIoAdapter.AdapterFactory getAdapterFactory() {
        return mAdapterFactory;
    }

    /**
     * Sets the adapter factory instance.
     *
     * @param adapterFactory Adapter factory instance to set or null for default.
     * @return Instance for chaining.
     * @throws IllegalStateException If instance is locked.
     */
    public SocketIoServerOptions setAdapterFactory(SocketIoAdapter.AdapterFactory adapterFactory) throws IllegalStateException {
        if (mIsLocked) {
            throw new IllegalStateException("Adapter factory cannot be set. Instance is locked.");
        }

        if (adapterFactory == null) {
            adapterFactory = MEMORY_ADAPTER_FACTORY;
        }

        mAdapterFactory = adapterFactory;
        return this;
    }

    /**
     * Lock this options instance to prevent modifications.
     */
    public void lock() {
        mIsLocked = true;
    }
}