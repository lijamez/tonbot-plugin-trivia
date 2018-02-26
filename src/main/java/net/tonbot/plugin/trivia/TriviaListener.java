package net.tonbot.plugin.trivia;

import net.tonbot.plugin.trivia.musicid.MusicIdQuestionEndEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionStartEvent;

public interface TriviaListener {

	void onRoundStart(RoundStartEvent roundStartEvent);

	void onRoundEnd(RoundEndEvent roundEndEvent);

	void onMultipleChoiceQuestionStart(MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent);

	void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent);

	void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent);

	void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent);

	void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent);

	void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent);

	void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent);

	void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent);
}
