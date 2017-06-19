<?php
$start = microtime(true);
error_reporting(E_ALL);
set_time_limit(0);
require_once("./DiDom/Document.php");
use DiDom\Document;

$url = "https://trashbox.ru/public/progs/tags/os_android";
$lastpage = file_get_contents("./last.page");
$_lastpage = $lastpage;
$apps = array();
$GLOBALS['apps'] = array();

echo('Processing ' . $lastpage . ' page...<br>');
$document = new Document($url, true);
$block = $document->find('.div_content_cat_topics .div_topic_cat_content');
get($block);
unset($block);
$lastpage = $lastpage - 1;
$url = $url . '/page_topics/';
while ($lastpage != 1) {
	echo('Processing ' . $lastpage . ' page...<br>');
	$document = new Document($url . $lastpage, true);
	$block = $document->find('.div_content_cat_topics .div_topic_cat_content');
	get($block);
	unset($block);
	$lastpage = $lastpage - 1;
}
$json = array(
	'LASTPAGE' => $_lastpage,
	'MEMUSAGE' => round(memory_get_peak_usage()/1024/1024,2) . " MB",
	'TIME' => (microtime(true)-$start)/60000000 . " мин.",
	'APPS' => $GLOBALS['apps']
);
echo json_encode($json, JSON_HEX_TAG | JSON_HEX_APOS | JSON_HEX_QUOT | JSON_HEX_AMP | JSON_UNESCAPED_UNICODE);

function get($block){
	foreach ($block as $sub){
		$title = $sub->first('a.a_topic_content span.div_topic_tcapt_content');
		$android = $sub->first('div.div_topic_cat_os_tags span');
		$tags = $sub->find('div.div_topic_cat_tags a');
		$taglist = array();
		foreach ($tags as $tag){
			array_push($taglist, $tag->text());
		}
		$link = $sub->find('a.download')[0]->attr('href');
		$dlink = new Document($link, true);
		if ($dlink != null) {
			if ($dlink->find('a.div_topic_top_download_button') != null) {
				$dlink = $dlink->find('a.div_topic_top_download_button')[0]->attr('href');
				$dlink = new Document($dlink, true);
				$temp = explode("'", $dlink->first('#div_landing_button_zone script')->text());
				$dlink = 'https://trashbox.ru/files20/' . trim(explode(",", $temp[2])[1]) . '_' . $temp[3] . '/' . $temp[5];
			} else {
				$dlink = "Error happend";
			}
		} else {
			$dlink = "err";
		}
		$adding = array(
		'TITLE' => $title->text(),
		'ANDROID' => $android->text(),
		'TAGS' => $taglist,
		'LINK' => $link,
		'DOWNLOADLINK' => $dlink
		);
		array_push($GLOBALS['apps'], $adding);
		unset($dlink);
		unset($temp);
	}
}