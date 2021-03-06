package net.tonbot.plugin.trivia;

import java.util.Optional;

import net.tonbot.plugin.trivia.model.Question;

public interface QuestionHandler {
	
	/**
	 * Gets the question.
	 * @return The {@link Question}.
	 */
	Question getQuestion();
	
	/**
	 * 
	 * @param questionNumber
	 *            The current question number.
	 * @param totalQuestions
	 *            The total number of questions to be asked.
	 * @param maxDurationMs
	 *            The maximum amount of time that will be waited in milliseconds.
	 */
	void notifyStart(long questionNumber, long totalQuestions, long maxDurationMs);

	/**
	 * Checks whether if the user's message answers the question.
	 * 
	 * @param userMessage
	 *            The {@link UserMessage}. Non-null.
	 * @return True if the message answers the question. False if the message
	 *         indicated the wrong answer. Empty if the answer didn't seem
	 *         applicable.
	 */
	Optional<Boolean> checkCorrectness(UserMessage userMessage);

	/**
	 * 
	 * @param userMessage
	 *            The user's message that ended the question. Nullable.
	 * @param awardedPoints
	 *            The number of points awarded to this answer.
	 * @param incorrectAttempts
	 *            The number of incorrect attempts made before getting the correct
	 *            answer.
	 */
	void notifyEnd(UserMessage userMessage, long awardedPoints, long incorrectAttempts);
}
