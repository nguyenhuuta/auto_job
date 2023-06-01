package com.autojob.api;

import com.autojob.model.entities.BaseResponse;
import com.autojob.utils.Logger;
import javafx.application.Platform;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.LinkedList;

public final class RequestQueue {

    public interface IRequestCallback<T> {
        default void onSuccess(T response) {
        }

        default void onFailure(String message) {
        }
    }

    public interface RequestHeaderCallback<T> {
        default void onSuccess(Headers headers, T response) {
        }

        default void onFailure(String message) {
        }
    }

    private static final String TAG = RequestQueue.class.getSimpleName();

    private final LinkedList<Request<?>> requestQueue;

    public static RequestQueue getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private RequestQueue() {
        requestQueue = new LinkedList<>();
    }

    public <T> void addRequest(Call<T> request, Callback<T> callback) {
        if (request != null) {
            Request<T> requestWrap = new Request<>(request, callback);
            requestQueue.add(requestWrap);
            if (requestQueue.size() == 1) {
                requestWrap.execute();
            }
        }
    }

    private final class Request<T> {

        private Call<T> request;

        private Callback<T> callback;

        private Request(Call<T> request, Callback<T> callback) {
            this.request = request;
            this.callback = callback;
        }

        private void execute() {
            request.enqueue(new CallbackDecorator<>(this));
        }
    }

    private final class CallbackDecorator<T> implements Callback<T> {

        private final Request<T> request;

        private final Callback<T> callback;

        private CallbackDecorator(Request<T> request) {
            this.request = request;
            this.callback = request.callback;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (callback != null) {
                callback.onResponse(call, response);
            }
            performNextRequest();
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (callback != null) {
                callback.onFailure(call, t);
            }
            performNextRequest();
        }

        private void performNextRequest() {
            requestQueue.remove(request);
            if (!requestQueue.isEmpty()) {
                Request<?> request = requestQueue.peek();
                request.execute();
            }
        }
    }

    private static final class InstanceHolder {

        private static final RequestQueue INSTANCE = new RequestQueue();

        private InstanceHolder() {
            //no instance
        }
    }

    /**
     * Chạy ở worked thread
     *
     * @param request
     * @param <T>
     * @return
     * @throws InterruptedException
     */
    public <T> T executeRequest(Call<BaseResponse<T>> request) throws InterruptedException {
        try {
            Response<BaseResponse<T>> response = request.execute();
            BaseResponse<T> responseData = response.body();
            if (responseData == null) {
                Logger.d("ExecuteRequest", response.toString());
                throw new InterruptedException("Response is NULL");
            }
            if (response.isSuccessful() && responseData.isSuccess) {
                return responseData.getData();
            } else {
                throw new InterruptedException("Có lỗi xảy ra");
            }
        } catch (IOException e) {
            throw new InterruptedException(e.toString());
        }
    }

    /**
     * Chạy ở MAIN THREAD
     *
     * @param request
     * @param callback
     * @param <T>
     */

    public <T> void enqueueRequest(Call<BaseResponse<T>> request, IRequestCallback<T> callback) {
        request.enqueue(new Callback<BaseResponse<T>>() {
            @Override
            public void onResponse(Call<BaseResponse<T>> call, Response<BaseResponse<T>> response) {
                Platform.runLater(() -> {
                    BaseResponse<T> responseData = response.body();
                    if (responseData == null) {
                        callback.onFailure("responseData null");
                    } else {
                        if (response.isSuccessful() && responseData.isSuccess) {
                            Platform.runLater(() -> callback.onSuccess(responseData.data));
                        } else {
                            callback.onFailure(response.toString());
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<BaseResponse<T>> call, Throwable throwable) {
                Platform.runLater(() -> callback.onFailure(throwable.toString()));
            }
        });
    }
}
