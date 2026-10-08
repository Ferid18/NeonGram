package com.neongroup.neogram.helpers;

import android.text.TextUtils;
import android.util.LongSparseArray;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_bots;

import java.util.ArrayList;

import com.neongroup.neogram.Extra;
import com.neongroup.neogram.NeoConfig;

import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class RelationshipHelper extends AccountInstance {

    public static final String METHOD_GET_PROFILE_RELATIONSHIP = "getProfileRelationship";
    public static final String METHOD_REGISTER_FCM_TOKEN = "registerFcmToken";

    public static final String TYPE_ROMANTIC = "romantic";
    public static final String TYPE_FRIEND = "friend";

    public static final String STATE_PENDING = "pending";
    public static final String STATE_ACTIVE = "active";
    public static final String STATE_ENDED = "ended";

    private static final long CACHE_TTL_MS = 60_000L;
    private static final RelationshipHelper[] Instance = new RelationshipHelper[UserConfig.MAX_ACCOUNT_COUNT];

    private final Gson gson = new Gson();
    private final LongSparseArray<CacheEntry> profileCache = new LongSparseArray<>();
    private final LongSparseArray<ArrayList<Utilities.Callback2<RelationshipProfile, String>>> pendingRequests = new LongSparseArray<>();
    private final OkHttpClient httpClient = new OkHttpClient();
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public static class RelationshipProfile {
        public long requestedUserId;
        public long partnerUserId;
        public String state = STATE_ACTIVE;
        public String type = TYPE_ROMANTIC;
        public String partnerFirstName;
        public String partnerLastName;
        public String partnerDisplayName;
        public boolean partnerVerified;
        public boolean partnerPremium;
        public long startedAt;
        public int durationDays;
        public boolean publicVisible = true;
        public boolean mutuallyConfirmed = true;
        public String note;
        public PaymentInfo payment;

        public boolean isActive() {
            return STATE_ACTIVE.equals(state);
        }

        public boolean isFriend() {
            return TYPE_FRIEND.equals(type);
        }

        public boolean isRomantic() {
            return TYPE_ROMANTIC.equals(type);
        }
    }

    public static class PaymentInfo {
        public boolean required;
        public boolean paid;
        public long starsAmount;
        public String currency;
        public long amount;
        public String status;
        public String invoiceSlug;
        public String invoicePayload;
        public String provider;
        public String title;
        public String description;
        public String buttonText;
        public String url;

        public boolean hasStarsPrice() {
            return starsAmount > 0 || ("XTR".equalsIgnoreCase(currency) && amount > 0);
        }
    }

    private static class CacheEntry {
        private final long loadedAt;
        private final RelationshipProfile profile;

        private CacheEntry(long loadedAt, RelationshipProfile profile) {
            this.loadedAt = loadedAt;
            this.profile = profile;
        }
    }

    private static class GetProfileRequest {
        @SerializedName("user_id")
        private final long userId;
        @SerializedName("viewer_user_id")
        private final long viewerUserId;

        private GetProfileRequest(long userId, long viewerUserId) {
            this.userId = userId;
            this.viewerUserId = viewerUserId;
        }
    }

    private static class RegisterFcmRequest {
        @SerializedName("user_id")
        private final long userId;
        @SerializedName("fcm_token")
        private final String fcmToken;

        private RegisterFcmRequest(long userId, String fcmToken) {
            this.userId = userId;
            this.fcmToken = fcmToken;
        }
    }

    private static class RelationshipPayload {
        @SerializedName(value = "state", alternate = {"relation_state"})
        private String state;
        @SerializedName(value = "type", alternate = {"relation_type", "kind", "status"})
        private String type;
        @SerializedName(value = "partner_user_id", alternate = {"partnerUserId", "other_user_id", "otherUserId"})
        private long partnerUserId;
        @SerializedName(value = "partner_first_name", alternate = {"partnerFirstName"})
        private String partnerFirstName;
        @SerializedName(value = "partner_last_name", alternate = {"partnerLastName"})
        private String partnerLastName;
        @SerializedName(value = "partner_display_name", alternate = {"partnerDisplayName", "partner_name", "partnerName"})
        private String partnerDisplayName;
        @SerializedName(value = "partner_verified", alternate = {"partnerVerified"})
        private Boolean partnerVerified;
        @SerializedName(value = "partner_premium", alternate = {"partnerPremium"})
        private Boolean partnerPremium;
        @SerializedName(value = "started_at", alternate = {"startedAt", "since_date", "sinceDate", "accepted_at", "acceptedAt"})
        private Long startedAt;
        @SerializedName(value = "duration_days", alternate = {"durationDays", "days_together", "daysTogether"})
        private Integer durationDays;
        @SerializedName(value = "public_visible", alternate = {"publicVisible"})
        private Boolean publicVisible;
        @SerializedName(value = "mutually_confirmed", alternate = {"mutuallyConfirmed"})
        private Boolean mutuallyConfirmed;
        @SerializedName(value = "note", alternate = {"description"})
        private String note;
        @SerializedName(value = "visibility", alternate = {"visibility_type"})
        private String visibility;
        @SerializedName(value = "payment", alternate = {"stars_payment", "starsPayment", "payment_info", "paymentInfo"})
        private PaymentPayload payment;
        @SerializedName(value = "payment_required", alternate = {"paymentRequired", "requires_payment", "requiresPayment"})
        private Boolean paymentRequired;
        @SerializedName(value = "payment_paid", alternate = {"paymentPaid", "paid", "is_paid", "isPaid"})
        private Boolean paymentPaid;
        @SerializedName(value = "payment_status", alternate = {"paymentStatus"})
        private String paymentStatus;
        @SerializedName(value = "stars_amount", alternate = {"starsAmount", "stars", "price_stars", "priceStars", "amount_stars", "amountStars"})
        private Long starsAmount;
        @SerializedName(value = "payment_currency", alternate = {"paymentCurrency", "currency"})
        private String paymentCurrency;
        @SerializedName(value = "payment_amount", alternate = {"paymentAmount", "amount", "price_amount", "priceAmount"})
        private Long paymentAmount;
        @SerializedName(value = "invoice_slug", alternate = {"invoiceSlug", "slug"})
        private String invoiceSlug;
        @SerializedName(value = "invoice_payload", alternate = {"invoicePayload", "payload"})
        private String invoicePayload;
        @SerializedName(value = "payment_url", alternate = {"paymentUrl", "url"})
        private String paymentUrl;
        @SerializedName(value = "payment_provider", alternate = {"paymentProvider", "provider"})
        private String paymentProvider;
    }

    private static class PaymentPayload {
        @SerializedName(value = "required", alternate = {"payment_required", "paymentRequired", "requires_payment", "requiresPayment"})
        private Boolean required;
        @SerializedName(value = "paid", alternate = {"payment_paid", "paymentPaid", "is_paid", "isPaid"})
        private Boolean paid;
        @SerializedName(value = "stars_amount", alternate = {"starsAmount", "stars", "price_stars", "priceStars", "amount_stars", "amountStars"})
        private Long starsAmount;
        @SerializedName(value = "currency", alternate = {"payment_currency", "paymentCurrency"})
        private String currency;
        @SerializedName(value = "amount", alternate = {"payment_amount", "paymentAmount", "price_amount", "priceAmount"})
        private Long amount;
        @SerializedName(value = "status", alternate = {"payment_status", "paymentStatus"})
        private String status;
        @SerializedName(value = "invoice_slug", alternate = {"invoiceSlug", "slug"})
        private String invoiceSlug;
        @SerializedName(value = "invoice_payload", alternate = {"invoicePayload", "payload"})
        private String invoicePayload;
        @SerializedName(value = "provider", alternate = {"payment_provider", "paymentProvider"})
        private String provider;
        @SerializedName(value = "title", alternate = {"payment_title", "paymentTitle"})
        private String title;
        @SerializedName(value = "description", alternate = {"payment_description", "paymentDescription"})
        private String description;
        @SerializedName(value = "button_text", alternate = {"buttonText", "payment_button_text", "paymentButtonText"})
        private String buttonText;
        @SerializedName(value = "url", alternate = {"payment_url", "paymentUrl"})
        private String url;
    }

    public RelationshipHelper(int num) {
        super(num);
    }

    public static RelationshipHelper getInstance(int num) {
        RelationshipHelper localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (RelationshipHelper.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new RelationshipHelper(num);
                }
            }
        }
        return localInstance;
    }

    public void getProfileRelationship(long userId, Utilities.Callback2<RelationshipProfile, String> callback) {
        getProfileRelationship(userId, false, callback);
    }

    public static void registerFCMToken(String token) {
        if (TextUtils.isEmpty(token)) return;

        AndroidUtilities.runOnUIThread(() -> {
            int account = UserConfig.selectedAccount;
            RelationshipHelper helper = RelationshipHelper.getInstance(account);
            long userId = helper.getUserConfig().getClientUserId();
            if (userId == 0) {
                FileLog.d("FCM token registration skipped: empty user id");
                return;
            }
            String requestPayload = helper.gson.toJson(new RegisterFcmRequest(userId, token));
            helper.invokeCustomMethod(METHOD_REGISTER_FCM_TOKEN, requestPayload, true, (response, error) -> {
                if (!TextUtils.isEmpty(error)) {
                    FileLog.d("FCM token registration failed: " + error);
                }
            });
        });
    }

    public void getProfileRelationship(long userId, boolean forceRefresh, Utilities.Callback2<RelationshipProfile, String> callback) {
        if (userId == 0) {
            if (callback != null) {
                AndroidUtilities.runOnUIThread(() -> callback.run(null, "EMPTY_USER_ID"));
            }
            return;
        }

        CacheEntry cacheEntry = profileCache.get(userId);
        if (!forceRefresh && cacheEntry != null && Math.abs(System.currentTimeMillis() - cacheEntry.loadedAt) < CACHE_TTL_MS) {
            if (callback != null) {
                AndroidUtilities.runOnUIThread(() -> callback.run(copyOf(cacheEntry.profile), null));
            }
            return;
        }

        ArrayList<Utilities.Callback2<RelationshipProfile, String>> callbacks = pendingRequests.get(userId);
        if (callbacks != null) {
            if (callback != null) {
                callbacks.add(callback);
            }
            return;
        }

        callbacks = new ArrayList<>();
        if (callback != null) {
            callbacks.add(callback);
        }
        pendingRequests.put(userId, callbacks);

        long viewerUserId = getUserConfig().getClientUserId();
        String requestPayload = gson.toJson(new GetProfileRequest(userId, viewerUserId));
        invokeCustomMethod(METHOD_GET_PROFILE_RELATIONSHIP, requestPayload, true, (response, error) -> {
            String responseError = !TextUtils.isEmpty(error) ? error : extractError(response);
            if (!TextUtils.isEmpty(responseError)) {
                finishProfileRequestWithFallback(userId, requestPayload, responseError);
                return;
            }

            finishProfileRequest(userId, response, null);
        });
    }

    private void finishProfileRequestWithFallback(long userId, String requestPayload, String primaryError) {
        invokeHttpProfileFallback(userId, requestPayload, (fallbackResponse, fallbackError) -> {
            if (!TextUtils.isEmpty(fallbackError)) {
                finishProfileRequest(userId, null, primaryError + ":" + fallbackError);
            } else {
                finishProfileRequest(userId, fallbackResponse, null);
            }
        });
    }

    private void finishProfileRequest(long userId, String response, String error) {
        RelationshipProfile profile = null;
        String finalError = error;
        if (finalError == null) {
            finalError = extractError(response);
            profile = parseProfileResponse(userId, response);
            if (profile != null) {
                profileCache.put(userId, new CacheEntry(System.currentTimeMillis(), copyOf(profile)));
            } else if (TextUtils.isEmpty(finalError)) {
                profileCache.put(userId, new CacheEntry(System.currentTimeMillis(), null));
            }
        }
        finishPending(userId, profile, finalError);
    }

    public void invalidate(long userId) {
        profileCache.remove(userId);
    }

    public void clearCache() {
        profileCache.clear();
    }

    private void finishPending(long userId, RelationshipProfile profile, String error) {
        ArrayList<Utilities.Callback2<RelationshipProfile, String>> callbacks = pendingRequests.get(userId);
        pendingRequests.remove(userId);
        if (callbacks == null) {
            return;
        }
        RelationshipProfile result = copyOf(profile);
        for (int i = 0; i < callbacks.size(); i++) {
            Utilities.Callback2<RelationshipProfile, String> callback = callbacks.get(i);
            if (callback != null) {
                callback.run(copyOf(result), error);
            }
        }
    }

    private RelationshipProfile parseProfileResponse(long requestedUserId, String response) {
        if (TextUtils.isEmpty(response) || "null".equalsIgnoreCase(response)) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(response);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonObject object = root.getAsJsonObject();
            JsonElement payloadElement = extractPayloadElement(object);
            if (payloadElement == null || payloadElement.isJsonNull() || !payloadElement.isJsonObject()) {
                return null;
            }

            RelationshipPayload payload = gson.fromJson(payloadElement, RelationshipPayload.class);
            JsonElement paymentElement = extractPaymentElement(object, payloadElement);
            if (paymentElement != null && paymentElement.isJsonObject()) {
                payload.payment = gson.fromJson(paymentElement, PaymentPayload.class);
            }
            RelationshipProfile profile = buildProfile(requestedUserId, payload);
            if (profile == null) {
                return null;
            }
            return fillFromLocalUser(profile);
        } catch (Exception e) {
            FileLog.e(e);
            return null;
        }
    }

    private JsonElement extractPayloadElement(JsonObject object) {
        if (object.has("relationship")) {
            return object.get("relationship");
        }
        if (object.has("data")) {
            JsonElement data = object.get("data");
            if (data != null && data.isJsonObject()) {
                JsonObject dataObject = data.getAsJsonObject();
                if (dataObject.has("relationship")) {
                    return dataObject.get("relationship");
                }
            }
            return data;
        }
        return object;
    }

    private JsonElement extractPaymentElement(JsonObject object, JsonElement payloadElement) {
        JsonElement paymentElement = getPaymentElement(payloadElement);
        if (paymentElement != null) {
            return paymentElement;
        }
        paymentElement = getPaymentElement(object);
        if (paymentElement != null) {
            return paymentElement;
        }
        if (object.has("data")) {
            JsonElement data = object.get("data");
            paymentElement = getPaymentElement(data);
            if (paymentElement != null) {
                return paymentElement;
            }
        }
        return null;
    }

    private JsonElement getPaymentElement(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("payment")) {
            return object.get("payment");
        }
        if (object.has("stars_payment")) {
            return object.get("stars_payment");
        }
        if (object.has("payment_info")) {
            return object.get("payment_info");
        }
        return null;
    }

    private String extractError(String response) {
        if (TextUtils.isEmpty(response)) {
            return null;
        }
        try {
            JsonElement root = JsonParser.parseString(response);
            if (!root.isJsonObject()) {
                return null;
            }
            JsonObject object = root.getAsJsonObject();
            if (object.has("ok") && !object.get("ok").getAsBoolean()) {
                if (object.has("error") && !object.get("error").isJsonNull()) {
                    return object.get("error").getAsString();
                }
                return "REQUEST_FAILED";
            }
            if (object.has("error") && !object.get("error").isJsonNull()) {
                String error = object.get("error").getAsString();
                if (!TextUtils.isEmpty(error)) {
                    return error;
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    private RelationshipProfile buildProfile(long requestedUserId, RelationshipPayload payload) {
        if (payload == null) {
            return null;
        }
        long startedAt = normalizeTimestamp(payload.startedAt != null ? payload.startedAt : 0L);
        int durationDays = payload.durationDays != null ? payload.durationDays : 0;
        long partnerUserId = payload.partnerUserId;

        boolean hasPayload = partnerUserId != 0
                || !TextUtils.isEmpty(payload.partnerDisplayName)
                || !TextUtils.isEmpty(payload.partnerFirstName)
                || !TextUtils.isEmpty(payload.partnerLastName)
                || startedAt > 0
                || durationDays > 0;
        if (!hasPayload) {
            return null;
        }

        RelationshipProfile profile = new RelationshipProfile();
        profile.requestedUserId = requestedUserId;
        profile.partnerUserId = partnerUserId;
        profile.state = normalizeState(payload.state);
        profile.type = normalizeType(payload.type);
        profile.partnerFirstName = payload.partnerFirstName;
        profile.partnerLastName = payload.partnerLastName;
        profile.partnerDisplayName = payload.partnerDisplayName;
        profile.partnerVerified = payload.partnerVerified != null && payload.partnerVerified;
        profile.partnerPremium = payload.partnerPremium != null && payload.partnerPremium;
        profile.startedAt = startedAt;
        profile.durationDays = durationDays > 0 ? durationDays : calculateDurationDays(startedAt);
        profile.publicVisible = payload.publicVisible == null ? !"private".equalsIgnoreCase(payload.visibility) : payload.publicVisible;
        profile.mutuallyConfirmed = payload.mutuallyConfirmed == null ? !STATE_PENDING.equals(profile.state) : payload.mutuallyConfirmed;
        profile.note = payload.note;
        profile.payment = buildPaymentInfo(payload);
        return profile;
    }

    private PaymentInfo buildPaymentInfo(RelationshipPayload payload) {
        if (payload == null) {
            return null;
        }

        PaymentPayload paymentPayload = payload.payment;
        boolean hasNestedPayment = paymentPayload != null && (
                paymentPayload.required != null
                        || paymentPayload.paid != null
                        || paymentPayload.starsAmount != null
                        || paymentPayload.amount != null
                        || !TextUtils.isEmpty(paymentPayload.currency)
                        || !TextUtils.isEmpty(paymentPayload.status)
                        || !TextUtils.isEmpty(paymentPayload.invoiceSlug)
                        || !TextUtils.isEmpty(paymentPayload.invoicePayload)
                        || !TextUtils.isEmpty(paymentPayload.provider)
                        || !TextUtils.isEmpty(paymentPayload.title)
                        || !TextUtils.isEmpty(paymentPayload.description)
                        || !TextUtils.isEmpty(paymentPayload.buttonText)
                        || !TextUtils.isEmpty(paymentPayload.url)
        );

        boolean hasFlatPayment = payload.paymentRequired != null
                || payload.paymentPaid != null
                || payload.starsAmount != null
                || payload.paymentAmount != null
                || !TextUtils.isEmpty(payload.paymentCurrency)
                || !TextUtils.isEmpty(payload.paymentStatus)
                || !TextUtils.isEmpty(payload.invoiceSlug)
                || !TextUtils.isEmpty(payload.invoicePayload)
                || !TextUtils.isEmpty(payload.paymentProvider)
                || !TextUtils.isEmpty(payload.paymentUrl);

        if (!hasNestedPayment && !hasFlatPayment) {
            return null;
        }

        PaymentInfo payment = new PaymentInfo();
        payment.required = firstBoolean(paymentPayload != null ? paymentPayload.required : null, payload.paymentRequired, false);
        payment.paid = firstBoolean(paymentPayload != null ? paymentPayload.paid : null, payload.paymentPaid, false);
        payment.starsAmount = firstLong(paymentPayload != null ? paymentPayload.starsAmount : null, payload.starsAmount, 0L);
        payment.amount = firstLong(paymentPayload != null ? paymentPayload.amount : null, payload.paymentAmount, 0L);
        payment.currency = firstString(paymentPayload != null ? paymentPayload.currency : null, payload.paymentCurrency);
        if (TextUtils.isEmpty(payment.currency) && payment.starsAmount > 0) {
            payment.currency = "XTR";
        }
        payment.status = firstString(paymentPayload != null ? paymentPayload.status : null, payload.paymentStatus);
        payment.invoiceSlug = firstString(paymentPayload != null ? paymentPayload.invoiceSlug : null, payload.invoiceSlug);
        payment.invoicePayload = firstString(paymentPayload != null ? paymentPayload.invoicePayload : null, payload.invoicePayload);
        payment.provider = firstString(paymentPayload != null ? paymentPayload.provider : null, payload.paymentProvider);
        payment.title = paymentPayload != null ? paymentPayload.title : null;
        payment.description = paymentPayload != null ? paymentPayload.description : null;
        payment.buttonText = paymentPayload != null ? paymentPayload.buttonText : null;
        payment.url = firstString(paymentPayload != null ? paymentPayload.url : null, payload.paymentUrl);
        return payment;
    }

    private RelationshipProfile fillFromLocalUser(RelationshipProfile profile) {
        if (profile == null || profile.partnerUserId == 0) {
            return profile;
        }
        TLRPC.User partner = getMessagesController().getUser(profile.partnerUserId);
        if (partner == null) {
            return profile;
        }
        if (TextUtils.isEmpty(profile.partnerFirstName)) {
            profile.partnerFirstName = partner.first_name;
        }
        if (TextUtils.isEmpty(profile.partnerLastName)) {
            profile.partnerLastName = partner.last_name;
        }
        if (TextUtils.isEmpty(profile.partnerDisplayName)) {
            String fullName = formatName(partner.first_name, partner.last_name);
            profile.partnerDisplayName = !TextUtils.isEmpty(fullName) ? fullName : partner.username;
        }
        profile.partnerVerified = profile.partnerVerified || partner.verified;
        profile.partnerPremium = profile.partnerPremium || partner.premium;
        return profile;
    }

    private void invokeCustomMethod(String method, String data, boolean searchUser, Utilities.Callback2<String, String> callback) {
        var botInfo = Extra.getHelperBot();
        if (botInfo == null) {
            if (callback != null) {
                callback.run(null, "EMPTY_BOT_INFO");
            }
            return;
        }
        TLRPC.User user = getMessagesController().getUser(botInfo.getId());
        if (user == null) {
            if (searchUser) {
                getUserHelper().resolveUser(botInfo.getUsername(), botInfo.getId(), arg -> invokeCustomMethod(method, data, false, callback));
            } else if (callback != null) {
                callback.run(null, "USER_NOT_FOUND");
            }
            return;
        }

        TL_bots.invokeWebViewCustomMethod req = new TL_bots.invokeWebViewCustomMethod();
        req.bot = getMessagesController().getInputUser(user);
        req.custom_method = method;
        req.params = new TLRPC.TL_dataJSON();
        req.params.data = data;
        getConnectionsManager().sendRequest(req, (res, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (callback == null) {
                return;
            }
            if (error != null) {
                callback.run(null, error.text);
            } else if (res instanceof TLRPC.TL_dataJSON json) {
                callback.run(json.data, null);
            } else {
                callback.run(null, null);
            }
        }));
    }

    private void invokeHttpProfileFallback(long userId, String data, Utilities.Callback2<String, String> callback) {
        String baseUrl = Extra.getHelperApiBaseUrl();
        if (TextUtils.isEmpty(baseUrl)) {
            if (callback != null) {
                callback.run(null, "EMPTY_HELPER_API_URL");
            }
            return;
        }
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        Request request = new Request.Builder()
                .url(normalizedBaseUrl + "/api/" + METHOD_GET_PROFILE_RELATIONSHIP)
                .post(RequestBody.create(data, JSON_MEDIA_TYPE))
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.run(null, e.getClass().getSimpleName());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : null;
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback == null) {
                        return;
                    }
                    if (response.isSuccessful()) {
                        callback.run(responseBody, null);
                    } else {
                        callback.run(null, "HTTP_" + response.code());
                    }
                });
            }
        });
    }

    private static long normalizeTimestamp(long value) {
        if (value <= 0) {
            return 0L;
        }
        return value < 10_000_000_000L ? value * 1000L : value;
    }

    private static int calculateDurationDays(long startedAt) {
        if (startedAt <= 0) {
            return 0;
        }
        long delta = Math.max(0L, System.currentTimeMillis() - startedAt);
        return Math.max(1, (int) (delta / (24L * 60L * 60L * 1000L)));
    }

    private static String normalizeType(String type) {
        if (TextUtils.isEmpty(type)) {
            return TYPE_ROMANTIC;
        }
        String normalized = type.trim().toLowerCase();
        if ("friend".equals(normalized) || "friends".equals(normalized) || "friendship".equals(normalized) || "close_friend".equals(normalized)) {
            return TYPE_FRIEND;
        }
        return TYPE_ROMANTIC;
    }

    private static String normalizeState(String state) {
        if (TextUtils.isEmpty(state)) {
            return STATE_ACTIVE;
        }
        String normalized = state.trim().toLowerCase();
        if ("pending".equals(normalized)) {
            return STATE_PENDING;
        }
        if ("ended".equals(normalized) || "expired".equals(normalized) || "cancelled".equals(normalized)) {
            return STATE_ENDED;
        }
        return STATE_ACTIVE;
    }

    private static String formatName(String firstName, String lastName) {
        if (TextUtils.isEmpty(firstName)) {
            return lastName;
        }
        if (TextUtils.isEmpty(lastName)) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private static boolean firstBoolean(Boolean primary, Boolean fallback, boolean defaultValue) {
        if (primary != null) {
            return primary;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }

    private static long firstLong(Long primary, Long fallback, long defaultValue) {
        if (primary != null) {
            return primary;
        }
        if (fallback != null) {
            return fallback;
        }
        return defaultValue;
    }

    private static String firstString(String primary, String fallback) {
        if (!TextUtils.isEmpty(primary)) {
            return primary;
        }
        return fallback;
    }

    private static RelationshipProfile copyOf(RelationshipProfile source) {
        if (source == null) {
            return null;
        }
        RelationshipProfile copy = new RelationshipProfile();
        copy.requestedUserId = source.requestedUserId;
        copy.partnerUserId = source.partnerUserId;
        copy.state = source.state;
        copy.type = source.type;
        copy.partnerFirstName = source.partnerFirstName;
        copy.partnerLastName = source.partnerLastName;
        copy.partnerDisplayName = source.partnerDisplayName;
        copy.partnerVerified = source.partnerVerified;
        copy.partnerPremium = source.partnerPremium;
        copy.startedAt = source.startedAt;
        copy.durationDays = source.durationDays;
        copy.publicVisible = source.publicVisible;
        copy.mutuallyConfirmed = source.mutuallyConfirmed;
        copy.note = source.note;
        copy.payment = copyOf(source.payment);
        return copy;
    }

    private static PaymentInfo copyOf(PaymentInfo source) {
        if (source == null) {
            return null;
        }
        PaymentInfo copy = new PaymentInfo();
        copy.required = source.required;
        copy.paid = source.paid;
        copy.starsAmount = source.starsAmount;
        copy.currency = source.currency;
        copy.amount = source.amount;
        copy.status = source.status;
        copy.invoiceSlug = source.invoiceSlug;
        copy.invoicePayload = source.invoicePayload;
        copy.provider = source.provider;
        copy.title = source.title;
        copy.description = source.description;
        copy.buttonText = source.buttonText;
        copy.url = source.url;
        return copy;
    }
}
