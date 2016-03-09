package com.solo.search.card;

import com.solo.search.card.entry.AdsEntry;
import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.CurrencyEntry;
import com.solo.search.card.entry.FunnyEntry;
import com.solo.search.card.entry.GameEntry;
import com.solo.search.card.entry.HotnewsEntry;
import com.solo.search.card.entry.HotwordEntry;
import com.solo.search.card.entry.StockEntry;
import com.solo.search.card.entry.VideoEntry;

import android.content.Context;
import android.database.Cursor;

/**
 * 
 * 卡片工厂，根据不同的卡片数据生成不同的卡片。
 */
public class CardFactory {

	public static BaseCard makeCard(Context context, CardEntry cardEntry) {
		String cardId = cardEntry.getCardId();
		BaseCard card = null;
		switch (cardId) {
		case CardConfig.CARD_ID_ADS:
			card = new AdsCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_HOTWORD:
			card = new HotwordCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_HOTNEWS:
			card = new HotnewsCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_VIDEO:
			card = new VideoCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_STOCK:
			card = new StockCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_CURRENCY:
			card = new CurrencyCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_GAME:
			card = new GameCard(context, cardEntry);
			break;
		case CardConfig.CARD_ID_FUNNY:
			card = new FunnyCard(context, cardEntry);
			break;
		}
		return card;
	}

	public static CardEntry makeCardEntry(Context context, String cardId, Cursor cursor) {
		CardEntry cardeEntry = null;
		switch (cardId) {
		case CardConfig.CARD_ID_ADS:
			cardeEntry = new AdsEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_HOTWORD:
			cardeEntry = new HotwordEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_HOTNEWS:
			cardeEntry = new HotnewsEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_VIDEO:
			cardeEntry = new VideoEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_STOCK:
			cardeEntry = new StockEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_CURRENCY:
			cardeEntry = new CurrencyEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_GAME:
			cardeEntry = new GameEntry(context, cursor);
			break;
		case CardConfig.CARD_ID_FUNNY:
			cardeEntry = new FunnyEntry(context, cursor);
			break;
		}
		return cardeEntry;
	}

}
