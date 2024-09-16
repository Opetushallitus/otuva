import {execSync} from 'node:child_process';

const REPOSITORY_NAME = "Opetushallitus/otuva"
const SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL = process.env.SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL
const ENVIRONMENT_NAME = process.env.ENVIRONMENT_NAME

async function main(): Promise<void> {
  const notes = generateReleaseNotes();
  if (notes.commits.length > 0) {
    const fullNotes = notes.commits.join('\n')
    console.log(fullNotes)

    await sendToSlack(formatSlackMessage(notes.header, fullNotes));
  } else {
    console.log("No changes found.");
  }
}

function formatSlackMessage(header, notes: string): object {
  return {
    blocks: [{
      type: "header",
      text: {
        type: "plain_text",
        text: header
      }
    }, {
      type: "section",
      text: {
        type: "mrkdwn",
        text: notes,
      }
    }]
  }
}

async function sendToSlack(message: object): Promise<void> {
  const response = await fetch(SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(message),
  });

  if (!response.ok) {
    throw new Error(`Failed to send message to Slack: ${response.statusText}`);
  }
}

type ReleaseNotes = {
  header: string;
  commits: string[];
}

function generateReleaseNotes(): ReleaseNotes {
  const tags = getLastNTags(2);
  if (tags.length < 2) {
    console.log("Not enough tags to compare.");
    return {header: "", commits: []};
  }

  const date = new Date(Number(tags[1].split('-')[2]) * 1000)
  const header = `🎁 Changes in ${REPOSITORY_NAME} ${ENVIRONMENT_NAME} deployment on ${date}`

  const releaseNotes: string[] = [];
  let prevTag = tags[0];

  for (let i = 1; i < tags.length; i++) {
    const currentTag = tags[i];
    const gitLogLine = getCommitsBetweenTags(currentTag, prevTag);
    if (gitLogLine) {
      for (const logLine of gitLogLine) {
        const space = logLine.indexOf(' ')
        const hash = logLine.substring(0, space)
        const message = linkifyMessage(logLine.substring(space + 1))
        releaseNotes.push(`\`<https://github.com/${REPOSITORY_NAME}/commit/${hash}|${hash}>\` ${message}`)
      }
    }
    prevTag = currentTag;
  }

  return {header, commits: releaseNotes};
}

function linkifyMessage(message: string): string {
  return message.replace(/OPHYK-(\d+)/g, '<https://jira.eduuni.fi/browse/OPHYK-$1|OPHYK-$1>');
}

function getLastNTags(n: number): string[] {
  const command = `git tag --list 'green-${ENVIRONMENT_NAME}-*' --sort=-creatordate | head -n ${n}`;
  const tags = runGitCommand(command);
  return tags.split('\n').filter(Boolean);
}

function getCommitsBetweenTags(tag1: string, tag2: string): string[] {
  const command = `git log ${tag1}..${tag2} --oneline`;
  return runGitCommand(command).split('\n');
}

function runGitCommand(command: string): string {
  try {
    return execSync(command, {encoding: 'utf-8'}).trim();
  } catch (error) {
    throw new Error(`Git command failed: ${(error as Error).message}`);
  }
}

main().catch(err => {
  console.error(err)
  process.exit(1)
});
