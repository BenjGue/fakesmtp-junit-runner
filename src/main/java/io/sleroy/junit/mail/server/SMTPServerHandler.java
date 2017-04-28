package io.sleroy.junit.mail.server;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import com.nilhcem.fakesmtp.core.ServerConfiguration;
import com.nilhcem.fakesmtp.core.exception.BindPortException;
import com.nilhcem.fakesmtp.core.exception.OutOfRangePortException;

/**
 * Starts and stops the SMTP server.
 *
 * @author Nilhcem
 * @since 1.0
 */
public class SMTPServerHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SMTPServerHandler.class);
	private final MailSaver mailSaver;
	private final MailListener myListener;
	private final SMTPServer smtpServer ;

	/**
	 * Instantiates a new SMTP server handler.
	 *
	 * @param configuration
	 *            the server configuration
	 */
	public SMTPServerHandler(ServerConfiguration configuration) {
		if (configuration.memoryModeEnabled()) {
			mailSaver = new MemoryMailSaver(configuration, configuration.getStorageCharset());
		} else {
			mailSaver = new DiskMailSaver(configuration, configuration.getStorageCharset());
		}

		myListener = new MailListener(mailSaver);
		smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(myListener),
				new SMTPAuthHandlerFactory());
	}

	/**
	 * Starts the server on the port and address specified in parameters.
	 *
	 * @param port
	 *            the SMTP port to be opened.
	 * @param bindAddress
	 *            the address to bind to. null means bind to all.
	 * @throws BindPortException
	 *             when the port can't be opened.
	 * @throws OutOfRangePortException
	 *             when port is out of range.
	 * @throws IllegalArgumentException
	 *             when port is out of range.
	 */
	public void startServer(int port, InetAddress bindAddress) throws BindPortException, OutOfRangePortException {
		LOGGER.debug("Starting server on port {}", port);
		try {
			smtpServer.setBindAddress(bindAddress);
			smtpServer.setPort(port);
			smtpServer.start();
		} catch (RuntimeException exception) {
			if (exception.getMessage().contains("BindException")) { // Can't
																	// open port
				LOGGER.error("{}. Port {}", exception.getMessage(), port);
				throw new BindPortException(exception, port);
			} else if (exception.getMessage().contains("out of range")) { // Port
																			// out
																			// of
																			// range
				LOGGER.error("Port {} out of range.", port);
				throw new OutOfRangePortException(exception, port);
			} else { // Unknown error
				LOGGER.error("", exception);
				throw exception;
			}
		}
	}

	/**
	 * Stops the server.
	 * <p>
	 * If the server is not started, does nothing special.
	 * </p>
	 */
	public void stopServer() {
		if (smtpServer.isRunning()) {
			LOGGER.debug("Stopping server");
			smtpServer.stop();
		}
	}

	/**
	 * Returns the {@code MailSaver} object.
	 *
	 * @return the {@code MailSaver} object.
	 */
	public MailSaver getMailSaver() {
		return mailSaver;
	}

	/**
	 * Returns the {@code SMTPServer} object.
	 *
	 * @return the {@code SMTPServer} object.
	 */
	public SMTPServer getSmtpServer() {
		return smtpServer;
	}
}