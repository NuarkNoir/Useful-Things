package com.eclipsesource.json;

import java.io.IOException;

class JsonLiteral extends JsonValue {
   private final String value;
   private final boolean isNull;
   private final boolean isTrue;
   private final boolean isFalse;

   JsonLiteral(String value) {
      this.value = value;
      this.isNull = "null".equals(value);
      this.isTrue = "true".equals(value);
      this.isFalse = "false".equals(value);
   }

   void write(JsonWriter writer) throws IOException {
      writer.writeLiteral(this.value);
   }

   public String toString() {
      return this.value;
   }

   public int hashCode() {
      return this.value.hashCode();
   }

   public boolean isNull() {
      return this.isNull;
   }

   public boolean isTrue() {
      return this.isTrue;
   }

   public boolean isFalse() {
      return this.isFalse;
   }

   public boolean isBoolean() {
      return this.isTrue || this.isFalse;
   }

   public boolean asBoolean() {
      return this.isNull ? super.asBoolean() : this.isTrue;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object == null) {
         return false;
      } else if (this.getClass() != object.getClass()) {
         return false;
      } else {
         JsonLiteral other = (JsonLiteral)object;
         return this.value.equals(other.value);
      }
   }
}
