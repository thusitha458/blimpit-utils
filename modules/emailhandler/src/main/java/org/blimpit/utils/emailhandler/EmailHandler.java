package org.blimpit.utils.emailhandler;

/**
 * A interface which describes email handling APIs
 */
public interface EmailHandler {

    /**
     * Send an email
     *
     * @param to      email of the recipient
     * @param subject Subject of the email
     * @param body    body of the email
     * @return send or not
     */
    boolean sendEmail(String to, String subject, String body);

    /**
     * Send an email with attachement
     *
     * @param to          email of the recipient
     * @param subject     Subject of the email
     * @param body        of the email
     * @param attachement attachement path
     * @return send or not
     */
    boolean sendEmailWithAttachment(String to, String subject, String attachement);


}
