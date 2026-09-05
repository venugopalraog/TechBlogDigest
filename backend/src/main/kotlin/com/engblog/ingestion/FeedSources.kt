package com.engblog.ingestion

import com.engblog.model.Source

/**
 * Feed URLs verified by fetching each one directly (see project notes).
 * Uber has no discoverable RSS/Atom feed for its engineering blog as of
 * this writing (eng.uber.com/feed/ and uber.com/blog/engineering/rss/
 * both 404/406) — left out until a working feed is found.
 */
val FEED_SOURCES: List<Source> = listOf(
    Source("netflix", "Netflix Tech Blog", "https://netflixtechblog.com/feed", "https://netflixtechblog.com"),
    Source("aws", "AWS Blog", "https://aws.amazon.com/blogs/aws/feed/", "https://aws.amazon.com/blogs/aws/"),
    Source("meta", "Meta Engineering", "https://engineering.fb.com/feed/", "https://engineering.fb.com"),
    Source("google-research", "Google Research", "https://research.google/blog/rss/", "https://research.google/blog/"),
    Source("microsoft", "Microsoft Engineering", "https://devblogs.microsoft.com/engineering-at-microsoft/feed/", "https://devblogs.microsoft.com/engineering-at-microsoft/"),
    Source("spotify", "Spotify Engineering", "https://engineering.atspotify.com/feed", "https://engineering.atspotify.com"),
    Source("slack", "Slack Engineering", "https://slack.engineering/feed/", "https://slack.engineering"),
    Source("reddit", "Reddit Engineering", "https://www.reddit.com/r/RedditEng/.rss", "https://www.reddit.com/r/RedditEng/"),
    Source("figma", "Figma Blog", "https://www.figma.com/blog/feed/atom.xml", "https://www.figma.com/blog/"),
    Source("openai", "OpenAI Blog", "https://openai.com/news/rss.xml", "https://openai.com/news/"),
    Source("cloudflare", "Cloudflare Blog", "https://blog.cloudflare.com/rss/", "https://blog.cloudflare.com"),
)
