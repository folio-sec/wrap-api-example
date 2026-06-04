<?php

declare(strict_types=1);

namespace Folio\CodingInterview\Domain;

final class BigDecimal
{
    private const SCALE = 20;

    private string $value;

    public function __construct(string|int|float $value)
    {
        if (is_int($value) || is_float($value)) {
            $value = (string) $value;
        }
        if (!preg_match('/^-?\d+(\.\d+)?$/', $value)) {
            throw new \InvalidArgumentException("invalid decimal: {$value}");
        }
        $this->value = $value;
    }

    public static function of(string|int|float|BigDecimal $v): BigDecimal
    {
        return $v instanceof BigDecimal ? $v : new BigDecimal($v);
    }

    public static function zero(): BigDecimal
    {
        return new BigDecimal('0');
    }

    public function add(BigDecimal $other): BigDecimal
    {
        return new BigDecimal(bcadd($this->value, $other->value, self::SCALE));
    }

    public function sub(BigDecimal $other): BigDecimal
    {
        return new BigDecimal(bcsub($this->value, $other->value, self::SCALE));
    }

    public function mul(BigDecimal $other): BigDecimal
    {
        return new BigDecimal(bcmul($this->value, $other->value, self::SCALE));
    }

    public function div(BigDecimal $other): BigDecimal
    {
        return new BigDecimal(bcdiv($this->value, $other->value, self::SCALE));
    }

    public function abs(): BigDecimal
    {
        return new BigDecimal(ltrim($this->value, '-') === '' ? '0' : ltrim($this->value, '-'));
    }

    public function compare(BigDecimal $other): int
    {
        return bccomp($this->value, $other->value, self::SCALE);
    }

    public function gt(BigDecimal $other): bool { return $this->compare($other) > 0; }
    public function lt(BigDecimal $other): bool { return $this->compare($other) < 0; }
    public function eq(BigDecimal $other): bool { return $this->compare($other) === 0; }

    /** Truncate toward zero (floor for non-negative) at given scale. */
    public function setScaleDown(int $scale): BigDecimal
    {
        return new BigDecimal(bcadd($this->value, '0', $scale));
    }

    public function toString(): string
    {
        return $this->stripTrailingZeros($this->value);
    }

    public function rawString(): string
    {
        return $this->value;
    }

    private function stripTrailingZeros(string $s): string
    {
        if (!str_contains($s, '.')) {
            return $s;
        }
        $s = rtrim($s, '0');
        $s = rtrim($s, '.');
        return $s === '' || $s === '-' ? '0' : $s;
    }

    public function __toString(): string
    {
        return $this->toString();
    }
}
