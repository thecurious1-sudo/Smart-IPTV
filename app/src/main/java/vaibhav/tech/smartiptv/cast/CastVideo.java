package vaibhav.tech.smartiptv.cast;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import vaibhav.tech.smartiptv.app.ChromecastApp;
import vaibhav.tech.smartiptv.media.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergey on 26.02.17.
 */
public class CastVideo {
    private CastSession mCastSession;
    private Context context;
    private ICastStatus statusListener = null;

    public void setStatusListener(ICastStatus statusListener) {
        this.statusListener = statusListener;
    }

    public CastVideo(Context context) {
        this.context = context;

        mCastSession = ChromecastApp.Instance().mCastContext.getSessionManager().getCurrentCastSession();
    }

    public void Cast(final Video video) {
        Log.d("PeformanceCast", "start build main video");
        final MediaInfo info = buildMediaInfo(video);
        Log.d("PeformanceCast", "end build main video");

        if (mCastSession == null) {
            return;
        }

        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {

            Log.d("ChromecastApp", "Remote media client is null");
            return;
        }

        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                if (remoteMediaClient.getMediaStatus() != null) {

                    Log.d("PlayStatus", "Current Status: " + remoteMediaClient.getMediaStatus().getPlayerState());

                    if (remoteMediaClient.getMediaStatus().getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING) {
                        if (info.getMediaTracks() != null) {
                            if (info.getMediaTracks().size() > 0) {
                                remoteMediaClient.setActiveMediaTracks(new long[]{1});
                                Log.d("Subtitle", "Start subtitle");
                                remoteMediaClient.removeListener(this);
                            }
                        }
                    }
                }
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
                if (remoteMediaClient.getMediaStatus() != null) {
                    Log.d("onQueueStatusUpdated", "Current Status: " + remoteMediaClient.getMediaStatus().getPlayerState());
                }
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {

            }
        });

        Log.d("PeformanceCast", "start build play list");
        List<MediaQueueItem> queue = new ArrayList<>();
        if (video.otherVideos != null) {
            for (Video other : video.otherVideos) {
                if (!TextUtils.isEmpty(other.getMimeType())) {
                    MediaQueueItem queueItem = new MediaQueueItem.Builder(buildMediaInfo(other))
                            .setAutoplay(true)
                            .setPreloadTime(10)
                            .build();

                    queue.add(queueItem);
                } else {
                    Log.d("MIME", "Mime null " + other.getUrl());
                }
            }
        }
        queue.clear();
        Log.d("PeformanceCast", "end build play list");
        if (queue.size() > 0) {
            Log.d("PeformanceCast", "start play quenue");
            MediaQueueItem queueItem = new MediaQueueItem.Builder(info)
                    .setAutoplay(true)
                    .setPreloadTime(0)
                    .build();
            queue.set(0, queueItem);
            PendingResult<RemoteMediaClient.MediaChannelResult> result = remoteMediaClient.queueLoad(queue.toArray(new MediaQueueItem[queue.size()]), 0, MediaStatus.REPEAT_MODE_REPEAT_OFF, null);

            result.setResultCallback(new ResolvingResultCallbacks<RemoteMediaClient.MediaChannelResult>((Activity) context, 1) {
                @Override
                public void onSuccess(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                    Log.d("onSuccess", "SUCCESS!!!");
                }

                @Override
                public void onUnresolvableFailure(@NonNull Status status) {
                    Log.d("onFailure", "FAILURE!!! " + status.getStatusMessage());
                    if (statusListener != null) {
                        statusListener.onError(video.getTitle());
                    }
                    remoteMediaClient.queueNext(null);
                }
            });
            Log.d("PeformanceCast", "end play quenue");
        } else {
            Log.d("PeformanceCast", "start play video");

            PendingResult<RemoteMediaClient.MediaChannelResult> result = remoteMediaClient.load(info, true, 0);
            result.setResultCallback(new ResolvingResultCallbacks<RemoteMediaClient.MediaChannelResult>((Activity) context, 1) {
                @Override
                public void onSuccess(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {
                    Log.d("onSuccess", "SUCCESS!!!");
                }

                @Override
                public void onUnresolvableFailure(@NonNull Status status) {
                    Log.d("onFailure", "FAILURE!!!");
                    if (statusListener != null) {

                        statusListener.onError(video.getTitle());
                    }
                }
            });
            Log.d("PeformanceCast", "end play video");
        }
    }

    private MediaInfo buildMediaInfo(Video video) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, video.getTitle());

        List<MediaTrack> tracks = new ArrayList<>();
        if (!TextUtils.isEmpty(video.getSubtitle())) {
            MediaTrack subtitle = new MediaTrack.Builder(1 /* ID */,
                    MediaTrack.TYPE_TEXT)
                    .setName("Subtitle")
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId(video.getSubtitle())
                    .setLanguage("en-US")
                    .build();

            tracks.add(subtitle);
        }
        if (TextUtils.isEmpty(video.getMimeType())) {
            video.setMimeType("video/mp4");
        }

        int media_stream_type = MediaInfo.STREAM_TYPE_BUFFERED;
        if (video.getMimeType().equalsIgnoreCase("application/x-mpegURL") ||
                video.getMimeType().equalsIgnoreCase("application/vnd.apple.mpegURL") ||
                video.getMimeType().equalsIgnoreCase("application/dash+xml") ||
                video.getMimeType().equalsIgnoreCase("application/vnd.ms-sstr+xml") ||
                video.getMimeType().equalsIgnoreCase("text/xml") ||
                video.getMimeType().equalsIgnoreCase("video/mp2t")
                ) {
            media_stream_type = MediaInfo.STREAM_TYPE_LIVE;
        }
        MediaInfo.Builder builder = new MediaInfo.Builder(video.getUrl())
                .setStreamType(media_stream_type)
                .setContentType(video.getMimeType())
                .setMetadata(movieMetadata);

        if (tracks.size() > 0) {
            builder.setMediaTracks(tracks);
        }

        return builder
                .build();
    }
}
