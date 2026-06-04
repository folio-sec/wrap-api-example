export class PresentationException extends Error {}

export class BadRequestException extends PresentationException {
  constructor(message: string) {
    super(message);
    this.name = "BadRequestException";
  }
}
