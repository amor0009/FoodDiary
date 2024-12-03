#include <jni.h>
#include <string.h>
#include <time.h>

// Преобразует дату из "dd.MM.yyyy" в "yyyy-MM-dd"
JNIEXPORT jstring JNICALL
Java_com_pms_fooddiary_utils_DateUtils_formatDateForAPI(JNIEnv *env, jobject thiz, jstring date) {
    const char *input_date = (*env)->GetStringUTFChars(env, date, 0);

    // Форматы входной и выходной даты
    const char *input_format = "%d.%m.%Y";
    const char *output_format = "%Y-%m-%d";
    char formatted_date[11];

    struct tm tm;
    memset(&tm, 0, sizeof(struct tm));
    strptime(input_date, input_format, &tm);
    strftime(formatted_date, sizeof(formatted_date), output_format, &tm);

    (*env)->ReleaseStringUTFChars(env, date, input_date);
    return (*env)->NewStringUTF(env, formatted_date);
}
