package com.stocknews.notify;

/**
 * Interface for notification channels.
 * Implementations send messages to Slack, Telegram, WhatsApp, etc.
 */
public interface Notifier {
    /**
     * Send a message to a notification channel.
     * @param channelKey Channel identifier (e.g., "slack", "telegram", "whatsapp")
     * @param message Message to send
     * @return true if successful, false otherwise
     */
    boolean send(String channelKey, String message);
}
