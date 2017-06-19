package com.eclipsesource.json;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JsonArray extends JsonValue implements Iterable {
   private final List values;

   public JsonArray() {
      this.values = new ArrayList();
   }

   public JsonArray(JsonArray array) {
      this(array, false);
   }

   private JsonArray(JsonArray array, boolean unmodifiable) {
      if (array == null) {
         throw new NullPointerException("array is null");
      } else {
         if (unmodifiable) {
            this.values = Collections.unmodifiableList(array.values);
         } else {
            this.values = new ArrayList(array.values);
         }

      }
   }

   /** @deprecated */
   @Deprecated
   public static JsonArray readFrom(Reader reader) throws IOException {
      return JsonValue.readFrom(reader).asArray();
   }

   /** @deprecated */
   @Deprecated
   public static JsonArray readFrom(String string) {
      return JsonValue.readFrom(string).asArray();
   }

   public static JsonArray unmodifiableArray(JsonArray array) {
      return new JsonArray(array, true);
   }

   public JsonArray add(int value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(long value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(float value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(double value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(boolean value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(String value) {
      this.values.add(Json.value(value));
      return this;
   }

   public JsonArray add(JsonValue value) {
      if (value == null) {
         throw new NullPointerException("value is null");
      } else {
         this.values.add(value);
         return this;
      }
   }

   public JsonArray set(int index, int value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, long value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, float value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, double value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, boolean value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, String value) {
      this.values.set(index, Json.value(value));
      return this;
   }

   public JsonArray set(int index, JsonValue value) {
      if (value == null) {
         throw new NullPointerException("value is null");
      } else {
         this.values.set(index, value);
         return this;
      }
   }

   public JsonArray remove(int index) {
      this.values.remove(index);
      return this;
   }

   public int size() {
      return this.values.size();
   }

   public boolean isEmpty() {
      return this.values.isEmpty();
   }

   public JsonValue get(int index) {
      return (JsonValue)this.values.get(index);
   }

   public List values() {
      return Collections.unmodifiableList(this.values);
   }

   public Iterator iterator() {
      final Iterator iterator = this.values.iterator();
      return new Iterator() {
         public boolean hasNext() {
            return iterator.hasNext();
         }

         public JsonValue next() {
            return (JsonValue)iterator.next();
         }

         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   void write(JsonWriter writer) throws IOException {
      writer.writeArrayOpen();
      Iterator iterator = this.iterator();

      for(boolean first = true; iterator.hasNext(); first = false) {
         if (!first) {
            writer.writeArraySeparator();
         }

         ((JsonValue)iterator.next()).write(writer);
      }

      writer.writeArrayClose();
   }

   public boolean isArray() {
      return true;
   }

   public JsonArray asArray() {
      return this;
   }

   public int hashCode() {
      return this.values.hashCode();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object == null) {
         return false;
      } else if (this.getClass() != object.getClass()) {
         return false;
      } else {
         JsonArray other = (JsonArray)object;
         return this.values.equals(other.values);
      }
   }
}
