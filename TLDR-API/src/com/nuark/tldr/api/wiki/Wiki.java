package com.nuark.tldr.api.wiki;

public class Wiki {

    Format format;
    Action action;
    
    public Wiki() {
        
    }
}

enum Format {
    dump, 
    json, 
    none, 
    php, 
    txt, 
    xml
}

enum Action {
    block, 
    createaccount, 
    delete, 
    edit, 
    emailuser, 
    expandtemplates, 
    feedcontributions, 
    feedrecentchanges, 
    feedwatchlist, 
    help, 
    login, 
    logout, 
    move, 
    opensearch, 
    options,
    parse,
    query, 
    rollback, 
    tokens, 
    unblock, 
    undelete, 
    upload,
    watch
}
