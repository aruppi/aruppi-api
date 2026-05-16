package com.jeluchu.features.news.mappers

import com.jeluchu.core.extensions.getListSafe
import com.jeluchu.core.extensions.getStringSafe
import com.jeluchu.core.extensions.parseRssDate
import com.jeluchu.features.news.models.NewEntity
import com.prof18.rssparser.model.RssChannel
import org.bson.Document

fun RssChannel.toNews(
    source: String,
    list: MutableList<NewEntity>
) = list.apply{
    items.forEach { item ->
        add(
            NewEntity(
                source = source,
                link = item.link.orEmpty(),
                title = item.title.orEmpty(),
                image = item.image.orEmpty(),
                categories = item.categories,
                content = item.content.orEmpty(),
                description = item.description.orEmpty(),
                sourceDescription = description.orEmpty(),
                date = item.pubDate?.parseRssDate().orEmpty()
            )
        )
    }
}

fun documentToNewsEntity(doc: Document) = NewEntity(
    link = doc.getStringSafe(key = "link"),
    date = doc.getStringSafe(key = "date"),
    title = doc.getStringSafe(key = "title"),
    image = doc.getStringSafe(key = "image"),
    source = doc.getStringSafe(key = "source"),
    content = doc.getStringSafe(key = "content"),
    description = doc.getStringSafe(key = "description"),
    categories = doc.getListSafe<String>(key = "categories"),
    sourceDescription = doc.getStringSafe(key = "sourceDescription")
)