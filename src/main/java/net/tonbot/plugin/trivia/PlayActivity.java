package net.tonbot.plugin.trivia;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestion;
import net.tonbot.plugin.trivia.model.Question;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestion;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

class PlayActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia play")
			.parameters(ImmutableList.of("trivia name"))
			.description("Starts a round in the current channel.")
			.build();

	private final IDiscordClient discordClient;
	private final TriviaSessionManager triviaSessionManager;
	private final BotUtils botUtils;

	@Inject
	public PlayActivity(
			IDiscordClient discordClient,
			TriviaSessionManager triviaSessionManager,
			BotUtils botUtils) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager,
				"triviaSessionManager must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		String triviaPackName = args.trim();

		if (StringUtils.isBlank(triviaPackName)) {
			throw new TonbotBusinessException("You need to specify a trivia pack name.");
		}

		try {
			TriviaSessionKey sessionKey = new TriviaSessionKey(
					event.getGuild().getLongID(),
					event.getChannel().getLongID());
			triviaSessionManager.createSession(sessionKey, triviaPackName, new TriviaListener() {

				@Override
				public void onRoundStart(RoundStartEvent roundStartEvent) {
					TriviaMetadata metadata = roundStartEvent.getTriviaMetadata();
					String msg = ":checkered_flag: Starting " + metadata.getName() + "...";
					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onRoundEnd(RoundEndEvent roundEndEvent) {
					Map<Long, Long> scores = roundEndEvent.getScores();
					StringBuilder sb = new StringBuilder();
					sb.append(":triangular_flag_on_post: Round finished!\n\n");

					List<Entry<Long, Long>> ranking = scores.entrySet().stream()
							.sorted((x, y) -> {
								return (int) (y.getValue() - x.getValue());
							})
							.collect(Collectors.toList());

					long highestScore = ranking.size() > 0 ? ranking.get(0).getValue() : 0;

					for (Entry<Long, Long> entry : ranking) {
						IUser user = discordClient.fetchUser(entry.getKey());
						String displayName = user.getDisplayName(event.getGuild());
						long score = entry.getValue();

						if (score == highestScore) {
							sb.append(":trophy: ");
						}
						sb.append(String.format("%s: %d points\n", displayName, score));
					}
					botUtils.sendMessageSync(event.getChannel(), sb.toString());
				}

				@Override
				public void onMultipleChoiceQuestionStart(
						MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
					EmbedBuilder eb = getQuestionEmbedBuilder(multipleChoiceQuestionStartEvent);
					MultipleChoiceQuestion mcQuestion = multipleChoiceQuestionStartEvent.getMultipleChoiceQuestion();
					eb.withTitle(mcQuestion.getQuestion());

					StringBuilder sb = new StringBuilder();
					List<Choice> choices = multipleChoiceQuestionStartEvent.getChoices();
					for (int i = 0; i < choices.size(); i++) {
						Choice choice = choices.get(i);
						sb.append(String.format("``%d``: %s\n", i, choice.getValue()));
					}
					eb.withDescription(sb.toString());

					botUtils.sendEmbed(event.getChannel(), eb.build());
				}

				@Override
				public void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
					Win win = multipleChoiceQuestionEndEvent.getWin().orElse(null);
					Choice correctChoice = multipleChoiceQuestionEndEvent.getCorrectChoice();

					String msg;
					if (win == null) {
						msg = String.format(":alarm_clock: Time's up! The correct answer was: **%s**",
								correctChoice.getValue());
					} else {
						IUser user = discordClient.fetchUser(win.getWinnerUserId());
						msg = String.format("**%s** wins %d points for the answer: **%s**",
								user.getDisplayName(event.getGuild()),
								win.getPointsAwarded(),
								correctChoice.getValue());
					}

					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
					EmbedBuilder eb = getQuestionEmbedBuilder(shortAnswerQuestionStartEvent);
					ShortAnswerQuestion saQuestion = shortAnswerQuestionStartEvent.getShortAnswerQuestion();
					eb.withTitle(saQuestion.getQuestion());

					botUtils.sendEmbed(event.getChannel(), eb.build());
				}

				@Override
				public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {
					Win win = shortAnswerQuestionEndEvent.getWin().orElse(null);
					String acceptableAnswer = shortAnswerQuestionEndEvent.getAcceptableAnswer();

					String msg;
					if (win == null) {
						msg = String.format(":alarm_clock: Time's up! The correct answer was: **%s**",
								acceptableAnswer);
					} else {
						IUser user = discordClient.fetchUser(win.getWinnerUserId());
						msg = String.format("**%s** wins %d points for the answer: **%s**",
								user.getDisplayName(event.getGuild()),
								win.getPointsAwarded(),
								win.getWinningMessage().getMessage());
					}

					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
					// TODO
					botUtils.sendMessageSync(event.getChannel(), musicIdQuestionStartEvent.toString());
				}

				@Override
				public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
					// TODO
					botUtils.sendMessageSync(event.getChannel(), musicIdQuestionEndEvent.toString());
				}

				@Override
				public void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent) {
					IMessage message = discordClient.getMessageByID(answerCorrectEvent.getMessageId());
					if (message != null) {
						message.addReaction(ReactionEmoji.of("✅"));
					}
				}

				@Override
				public void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent) {
					IMessage message = discordClient.getMessageByID(answerIncorrectEvent.getMessageId());
					if (message != null) {
						message.addReaction(ReactionEmoji.of("❌"));
					}
				}

				private EmbedBuilder getQuestionEmbedBuilder(QuestionStartEvent qse) {
					EmbedBuilder eb = new EmbedBuilder();
					Question question = qse.getQuestion();

					eb.withFooterText(String.format("First to correctly answer within %d seconds wins %d points",
							qse.getMaxDurationSeconds(), qse.getQuestion().getPoints()));

					eb.withAuthorName(String.format("Question %d of %d",
							qse.getQuestionNumber(),
							qse.getTotalQuestions()));

					question.getImageUrl().ifPresent(imgUrl -> eb.withImage(imgUrl));

					return eb;
				}

			});
		} catch (InvalidTriviaPackException e) {
			throw new TonbotBusinessException("Invalid trivia pack name.");
		}

	}
}