<?php
require_once("./DiDom/Document.php");
use DiDom\Document;
$url = 'https://trashbox.ru/public/progs/tags/os_android/';
$document = new Document(file_get_contents($url));
$lastpage = $document->find('.span_navigator_pages .span_item_active');
$lastpage = $lastpage[0]->text();
echo "Спасибо за апдейт\nLP: $lastpage";
file_put_contents("last.page", $lastpage);
