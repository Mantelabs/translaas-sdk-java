package io.translaas.i18n;

/** CLDR plural categories for offline entry resolution. */
public enum PluralCategory {
  ZERO("zero"),
  ONE("one"),
  TWO("two"),
  FEW("few"),
  MANY("many"),
  OTHER("other");

  private final String wireName;

  PluralCategory(String wireName) {
    this.wireName = wireName;
  }

  public String wireName() {
    return wireName;
  }
}
