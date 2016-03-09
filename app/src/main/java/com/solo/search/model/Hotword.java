package com.solo.search.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Hotword implements Parcelable {

	private String mType;
	private String mText;
	private String mImgUrl;
	private String mUrl;

	private int mSize;
	private float mPositionX;
	private float mPositionY;

	public Hotword() {
	}

	public Hotword(String type, String text, String imgUrl, String url) {
		mType = type;
		mText = text;
		mImgUrl = imgUrl;
		mUrl = url;
	}

	public String getType() {
		return mType;
	}

	public String getText() {
		return mText;
	}

	public void setImageUrl(String imageUrl) {
		mImgUrl = imageUrl;
	}

	public String getImageUrl() {
		return mImgUrl;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setSize(int size) {
		mSize = size;
	}

	public int getSize() {
		return mSize;
	}

	public void setPositionX(float positionX) {
		mPositionX = positionX;
	}

	public float getPositionX() {
		return mPositionX;
	}

	public void setPositionY(float positionY) {
		mPositionY = positionY;
	}

	public float getPositionY() {
		return mPositionY;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mType);
		dest.writeString(mText);
		dest.writeString(mImgUrl);
		dest.writeString(mUrl);
		dest.writeInt(mSize);
		dest.writeFloat(mPositionX);
		dest.writeFloat(mPositionY);
	}

	public static final Parcelable.Creator<Hotword> CREATOR = new Creator<Hotword>() {
		@Override
		public Hotword createFromParcel(Parcel source) {
			Hotword mPerson = new Hotword();
			mPerson.mType = source.readString();
			mPerson.mText = source.readString();
			mPerson.mImgUrl = source.readString();
			mPerson.mUrl = source.readString();
			mPerson.mSize = source.readInt();
			mPerson.mPositionX = source.readFloat();
			mPerson.mPositionY = source.readFloat();
			return mPerson;
		}

		@Override
		public Hotword[] newArray(int size) {
			return new Hotword[size];
		}
	};

}
