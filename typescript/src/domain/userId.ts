export class UserId {
  readonly value: string;

  constructor(value: string) {
    if (value.length === 0) {
      throw new Error("userId must not be empty");
    }
    this.value = value;
  }
}
