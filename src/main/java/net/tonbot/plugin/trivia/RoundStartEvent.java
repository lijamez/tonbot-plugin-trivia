package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.model.TriviaMetadata;

@Data
@Builder
class RoundStartEvent {

	/**
	 * The trivia that just started.
	 */
	@NonNull
	private final TriviaMetadata triviaMetadata;

	@NonNull
	private final String difficultyName;
	
	private final long startingInMs;
	
	private final boolean hasAudio;
}
