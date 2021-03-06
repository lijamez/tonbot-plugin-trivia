package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
public class Win {

	private final Long pointsAwarded;
	private final long incorrectAttempts;
	private final UserMessage winningMessage;

	@Builder
	private Win(Long pointsAwarded, Long incorrectAttempts, UserMessage winningMessage) {
		this.pointsAwarded = Preconditions.checkNotNull(pointsAwarded, "pointsAwarded must be non-null.");
		this.incorrectAttempts = Preconditions.checkNotNull(incorrectAttempts, "incorrectAttempts must be non-null.");
		this.winningMessage = Preconditions.checkNotNull(winningMessage, "winningMessage must be non-null.");
	}

	public long getWinnerUserId() {
		return winningMessage.getUserId();
	}
}
