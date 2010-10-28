package com.amay077.android.followermap;

import android.os.AsyncTask;

public interface TaskFactory {
	@SuppressWarnings("unchecked")
	public abstract AsyncTask CreateTask();
}
