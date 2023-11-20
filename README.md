# BackPack

[![Java CI with Maven](https://github.com/fredie04/BackPack/actions/workflows/maven.yml/badge.svg)](https://github.com/fredie04/BackPack/actions/workflows/maven.yml) [![CodeQL](https://github.com/fredie04/BackPack/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/fredie04/BackPack/actions/workflows/codeql-analysis.yml)

## Introduction

Backpack is a spigot plugin to open shulker like normal backpack. Just right click the air with a shulker in the hand,
and you can interact like a placed shulker chest. It's also provides few security function which are free
configurationable.

## Using

Take a shulker chest and right click in the middle of the air. It's opens the shulker as an inventory which is
interactable. By closing the inventory the content getting safed. A direct saving isn't provided yet.

## Building/Installing

Build the project with Maven and move it into the plugin folder. The config.yml contains various settings options like
the filter, messages or permissions.

## Futures

* Realtime interaction with shulker inventory
* Security feature to disable illegal options
    * Direct filter to block action which contains a deployment of a forbidden item into the inventory
    * Force filter which scans the shulker inventory and move forbidden items to the player inventory

## Permissions

* `backpacks.using.bypassFiltering` if true, it will bypass the direct filter option
* `backpacks.using.bypassForceFilter` if true, it will don't check the shulker for illegal items
