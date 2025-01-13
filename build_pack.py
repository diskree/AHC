import os
import re
from pathlib import Path

NAME_COLOR_FROM = "#07c5fa"
NAME_COLOR_TO = "#b2e8f7"

VERSION_COLOR_MAJOR = "#d4b73a"
VERSION_COLOR_MINOR = "#edd362"
VERSION_COLOR_PATCH = "#d4b73a"

pack_name, pack_version, author, repo_url, minecraft_version, loader_version = (
    os.sys.argv[1:7]
)

pack_dir = "pack"

for file in Path(pack_dir).glob("*.mrpack"):
    file.unlink()

os.chdir(pack_dir)

word_center = pack_name
word_start_array = []
word_end_array = []

while word_center:
    word_center_len = len(word_center)
    if word_center_len in {1, 3, 4}:
        piece = 1
    else:
        piece = 2
    if word_center_len == piece:
        break
    word_start_array.append(word_center[:piece])
    word_end_array.append(word_center[-piece:])
    word_center = word_center[piece:-piece]

name_color_steps = len(word_start_array)

def hex_to_rgb(hex_color):
    return tuple(int(hex_color[i:i + 2], 16) for i in (1, 3, 5))

def rgb_to_hex(rgb):
    return f"#{rgb[0]:02x}{rgb[1]:02x}{rgb[2]:02x}"

from_rgb = hex_to_rgb(NAME_COLOR_FROM)
to_rgb = hex_to_rgb(NAME_COLOR_TO)

name_colors = [
    rgb_to_hex(
        (
            from_rgb[0] + (to_rgb[0] - from_rgb[0]) * i // name_color_steps,
            from_rgb[1] + (to_rgb[1] - from_rgb[1]) * i // name_color_steps,
            from_rgb[2] + (to_rgb[2] - from_rgb[2]) * i // name_color_steps,
        )
    )
    for i in range(name_color_steps + 1)
]

colored_pack_name = ""
for i, part in enumerate(word_start_array):
    color_index = i % (len(name_colors) - 1)
    colored_pack_name += f"&{{{name_colors[color_index]}}}{part}"

colored_pack_name += f"&{{{name_colors[-1]}}}{word_center}"

for i, part in reversed(list(enumerate(word_end_array))):
    color_index = i % (len(name_colors) - 1)
    colored_pack_name += f"&{{{name_colors[color_index]}}}{part}"

major, minor, patch = (pack_version.split('.') + ["", ""])[:3]
colored_pack_version = (
    f"&{{{VERSION_COLOR_MAJOR}}}{major}" if major else ""
) + (f".&{{{VERSION_COLOR_MINOR}}}{minor}" if minor else "") + (
    f".&{{{VERSION_COLOR_PATCH}}}{patch}" if patch else ""
)

def replace_in_file(file_path, replacements):
    file = Path(file_path)
    original_content = file.read_text()

    updated_content = original_content
    for placeholder, value in replacements.items():
        updated_content = re.sub(re.escape(f"${{{placeholder}}}"), value, updated_content)
    file.write_text(updated_content)

    return original_content


file_replacements = {
    "pack.toml": {
        "packName": pack_name,
        "author": author,
        "packVersion": pack_version,
        "loaderVersion": loader_version,
        "minecraftVersion": minecraft_version,
    },
    "config/yosbr/config/custom-hud/profile1.txt": {
        "coloredPackName": colored_pack_name,
        "coloredPackVersion": colored_pack_version,
    },
    "config/yosbr/config/isxander-main-menu-credits.json": {
        "packName": pack_name,
        "packVersion": pack_version,
        "repoUrl": repo_url,
    },
}

original_contents = {}
for file_path, replacements in file_replacements.items():
    original_contents[file_path] = replace_in_file(file_path, replacements)

os.system("packwiz refresh --build")
os.system("packwiz modrinth export")
os.system("packwiz refresh")

for file_path, original_content in original_contents.items():
    Path(file_path).write_text(original_content)
