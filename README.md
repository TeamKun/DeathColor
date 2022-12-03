# 死の色がランダムに変わるプラグイン

![イメージ図](https://cdn.discordapp.com/attachments/611227726971404298/1048612635165343754/image.png)

死の色がランダムに変わり、乗ったり触れたりした場合に死にます。

## 使い方

- `/deathcolor start` でゲームを開始します。
- `/deathcolor stop` でゲームを終了します。

## 仕様

以下が対応しています
- ブロック (Materialクラスにある全てのブロックに対応しています)
- アイテム (Materialクラスにある全てのブロックに対応しています)
- Mob (色付きの羊や、牛、コウモリなど、EntityTypeクラスにあるほぼ全てのMobに対応しています)
- エンティティ (ガストの玉や、ドロップアイテム、落下中のブロックなど、EntityTypeクラスにあるほぼ全てのEntityに対応しています、ウサギとネコ、ウマは対応していません)

以下の条件で死にます
- ブロックに乗る (プレイヤーの中心座標の真下のブロック)
- ブロックに埋まる (上記ブロックの1マス上のブロック)
- ブロックを右クリック/左クリック
- アイテムを手に持つ
- Mob/エンティティを殴る
- Mob/エンティティと接触する (プレイヤーのBoundingBoxがMobに触れたらアウトです)
- 装備を着る
- 雨(青色)/雪(白色)に当たる

フェーズについて  
以下の順番でフェーズが進行します
- カウントダウンフェーズ: 開始前のカウントダウンです。  
4秒死ぬ色が表示されたあと、5秒(設定可能)カウントダウンします。
- プレイフェーズ: 死ぬ色が表示され、死ぬ色に触れると死にます。  
1分、3分、5分(設定可能)の中からランダムな時間で終了します。
- 休憩時間  
15秒(設定可能)休憩します。

詳細な仕様
- リスポーンすると一定時間無敵モードになります
- 一部色判定が特殊なエンティティがあり、以下の色が使用されます
    - 額縁は中のアイテムの色
    - 落下中のブロックはブロックの色
    - ドロップアイテムはアイテムの色
    - 羊は毛の色
    - 色付きシュルカーの色
- ブロックの縁に乗っても死にます  
- ブロック色の抽選はランダムですが、16ターンで必ず
- 次の色を強制的に設定することができます
    - `/deathcolor setNextColor` で次の色を設定します。

## コンフィグ
- `cooldownTime`: 開始前の待機秒数
- `intervalTime`: 終了後の休憩秒数
- `respawnCooldownTime`: リスポーン後の無敵秒数
- `damage`: 死の色に触ったときのダメージ量
- `remainingTimeCandidates`: プレイ中の残り秒数の候補
    - デフォルトでは1分、3分、5分の中からランダム


## 開発者向け

ブロックの色対応をすべて手動で作るのは大変なため、ツールを使用して自動化しています。

### テクスチャ→色の変換ツールについて

Pythonのスクリプトでパスと色のマッピングを自動で作成しています。  

使い方
1. [`block_colors/textures`](./block_colors/textures/)にマイクラのテクスチャを入れる  
    - `assets/minecraft/textures`からテクスチャをコピペ
2. Python環境を用意する。  
    - `virtualenv`をインストールしてください。
3. [`block_colors`](./block_colors/)に移動して[`install.bat`](./block_colors/install.bat)を実行する
4. `python src/extract.py`を実行する
5. [`block_colors/textures_to_colors.json`](./block_colors/texture_to_color.json)にパス→色の対応表が出力される

ツールの出力結果は[こちら](./block_colors/texture_to_color.json)
```
{
    "block/acacia_door_bottom.png": "brown",
    "block/acacia_door_top.png": "orange",
    "block/acacia_leaves.png": "light_gray",
    ... 以下略 ...
}
```

### ブロック/エンティティ→色の変換ツールについて

パス→色の対応表をもとに、ブロック→色の対応表を作成しています。  
こちらはプラグインの中に組み込んであります。  

使い方
1. 前工程のツールで作成した[`block_colors/textures_to_colors.json`](./block_colors/texture_to_color.json)があることを確認する
2. マイクラで`/deathcolor debug_generate_material_to_color generateMaterial`を実行する
3. [`block_colors/material_to_color.yaml`](./block_colors/material_to_color.yml)にブロック→色の対応表が出力される
3. マイクラで`/deathcolor debug_generate_material_to_color generateEntity`を実行する
4. [`block_colors/entity_to_color.yaml`](./block_colors/entity_to_color.yml)にエンティティ→色の対応表が出力される

```
AIR: 'null'
STONE: brown
GRANITE: red
POLISHED_GRANITE: red
DIORITE: white
... 以下略 ...
```


### ブロック/エンティティの対応調整ツールについて

自動変換のみだと、ブロックの色が正しく判定されない場合があります。  
そのため、目視で確認できるようにマイクラ内で配置するツールを用意しています。  
このツールを使用することで、マイクラ内でブロック→色の対応がおかしい場合、手動で修正することができます。

![イメージ図](https://cdn.discordapp.com/attachments/611227726971404298/1048679468572614807/image.png)
![全体図](https://cdn.discordapp.com/attachments/611227726971404298/1048679494183030835/image.png)

使い方
1. 前工程のツールで作成した[`block_colors/material_to_color.yaml`](./block_colors/material_to_color.yml)、[`block_colors/entity_to_color.yaml`](./block_colors/entity_to_color.yml)を[`run/plugins/DeathColor/`](./run/plugins/DeathColor/)にコピーする
2. マイクラで`/deathcolor debug_generate_material_to_color material`を実行すると、マップ上にブロックが配置される
3. 修正したいプロットの羊毛の上に正しい色の羊毛を置き、`/deathcolor debug_generate_material_to_color saveMaterial`を実行することで、yamlファイルが更新される
4. 同様にエンティティも、`/deathcolor debug_generate_material_to_color entity`で配置でき、`/deathcolor debug_generate_material_to_color saveEntity`でyamlファイルを更新できる