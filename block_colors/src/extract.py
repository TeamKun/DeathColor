from colorthief import ColorThief
from colormath.color_objects import sRGBColor, LabColor
from colormath.color_diff import delta_e_cie2000
from colormath.color_conversions import convert_color
import numpy
import os
import json


# colormathで発生するエラーのワークアラウンド
# https://github.com/gtaylor/python-colormath/issues/104#issuecomment-1247481638
def patch_asscalar(a):
    return a.item()

setattr(numpy, "asscalar", patch_asscalar)


# マイクラの羊毛色
minecraft_palette = dict(
    white=(255, 255, 255),
    orange=(216, 127, 51),
    magenta=(178, 76, 216),
    light_blue=(102, 153, 216),
    yellow=(229, 229, 51),
    lime=(127, 204, 25),
    pink=(242, 127, 165),
    gray=(76, 76, 76),
    light_gray=(153, 153, 153),
    cyan=(76, 127, 153),
    purple=(127, 63, 178),
    blue=(51, 76, 178),
    brown=(102, 76, 51),
    green=(102, 127, 51),
    red=(153, 51, 51),
    black=(25, 25, 25),
)


# 一番近い色を探す
def closest_color(color):
    # Lab色空間に変換
    color_lab = convert_color(sRGBColor(color[0], color[1], color[2]), LabColor)
    # 色の差を保存するリスト
    color_diffs = []
    # パレットの色を一つずつ取り出す
    for palette_name, palette_color in minecraft_palette.items():
        # パレット色をLab色空間に変換
        palette_color_lab = convert_color(sRGBColor(palette_color[0], palette_color[1], palette_color[2]), LabColor)
        # 色の差を計算
        color_diff = delta_e_cie2000(color_lab, palette_color_lab)
        # 色の差をリストに追加
        color_diffs.append((color_diff, palette_name))
    # 色の差が一番小さい色を返す
    return min(color_diffs, key=lambda t: t[0])[1]


# 画像→色のマップ
texture_to_color = dict()

# 画像フォルダーのパス
textures_dir = 'textures'
# 再帰的にファイルを取得
for subdir, dirs, files in os.walk(textures_dir):
    for file in files:
        # pngファイルのみ
        if not file.endswith('.png'):
            continue

        # 画像ファイルのパス
        texture_path = os.path.join(subdir, file)
        # 特徴色を取得
        try:
            # 画像から特徴色を取得
            color_thief = ColorThief(texture_path)
            dominant_color = color_thief.get_color(quality=1)
            # 一番近い色を取得
            color_name = closest_color(dominant_color)
        except:
            # エラー
            color_name = 'null'

        # 画像の相対パス
        texture_relative_path = os.path.relpath(texture_path, textures_dir).replace('\\','/')
        # 出力
        print(texture_relative_path, color_name)
        # 画像→色のマップに追加
        texture_to_color[texture_relative_path] = color_name

# JSONとしてファイルに出力
with open('texture_to_color.json', 'w') as f:
    json.dump(texture_to_color, f, indent=4, ensure_ascii=False)
