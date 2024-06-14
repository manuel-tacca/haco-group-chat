package project.Communication.Messages;

/**
 * The MessageCausalityStatus enum represents the possible statuses
 * of a message with regard to its causality in the system. It indicates
 * whether a message has been accepted, discarded, or queued for processing.
 */
public enum MessageCausalityStatus {
    ACCEPTED,
    DISCARDED,
    QUEUED
}
