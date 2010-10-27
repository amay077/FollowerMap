package com.amay077.android;

import android.os.AsyncTask;

public interface TaskFactory {
	@SuppressWarnings("unchecked")
	public abstract AsyncTask CreateTask();
}
