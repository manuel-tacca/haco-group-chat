package project.Model;

/**
 * This record represents a notification that is sent from the controller or from the communication system to the view.
 *
 * @param type The type of the notification.
 * @param content The content of the notification.
 */
public record Notification(NotificationType type, String content){}