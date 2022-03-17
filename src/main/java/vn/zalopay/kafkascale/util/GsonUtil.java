package vn.zalopay.kafkascale.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Base64;

public class GsonUtil {

  private static final Gson gson;
  private static final Gson gsonSnakeCase;

  public static String toJsonString(Object obj) {
    return gson.toJson(obj);
  }

  public static <T> T fromJsonString(String sJson, Class<T> t) {
    return gson.fromJson(sJson, t);
  }

  public static <T> T fromJsonString(String sJson, Class<T> t, T defaultValue) {
    try {
      return gson.fromJson(sJson, t);
    } catch (Exception ex) {
      return defaultValue;
    }
  }

  public static String toJsonStringSnakeCase(Object object) {
    return gsonSnakeCase.toJson(object);
  }

  public static <T> T fromJsonSnakeCase(String jsonStringSnakeCase, Class<T> clazz) {
    return gsonSnakeCase.fromJson(jsonStringSnakeCase, clazz);
  }

  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    gsonBuilder.registerTypeAdapter(byte[].class, new TypeAdapter<byte[]>() {
      @Override
      public void write(JsonWriter out, byte[] value) throws IOException {
        out.value(Base64.getEncoder().encodeToString(value));
      }

      @Override
      public byte[] read(JsonReader in) throws IOException {
        return Base64.getDecoder().decode(in.nextString());
      }
    });
    gson = gsonBuilder.disableHtmlEscaping().create();

    GsonBuilder snakeCaseGsonBuilder = new GsonBuilder();
    snakeCaseGsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    snakeCaseGsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    gsonSnakeCase = snakeCaseGsonBuilder.create();
  }
}
