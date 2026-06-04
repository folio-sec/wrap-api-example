<?php

declare(strict_types=1);

require __DIR__ . '/../../../../vendor/autoload.php';

use Folio\CodingInterview\Infrastructure\Server\DummyServer;

DummyServer::default();
echo "DummyServer initialized.\n";
