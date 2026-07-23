from pathlib import Path


ROOT = Path("src/main/resources/assets/dream_sakura_blue_archive/models/obj")
SOURCES = {
    "right": ROOT / "tendouaris_sword_of_light.obj",
    "left": ROOT / "tendouaris_sword_of_light_left.obj",
}

GROUPS = {
    "static": {
        "主体.002",
        "盖板.002",
        "辅助握柄.002",
        "液压杆1.002",
        "液压杆2.002",
    },
    "upper": {
        "上盖板1.002",
    },
    "upper_small": {
        "上盖板2.002",
    },
    "upper_rod_head": set(),
    "lower": {
        "下盖板1.002",
    },
    "lower_small": {
        "下盖板2.002",
    },
    "lower_rod_head": set(),
    "glow": {
        "发光.002",
    },
    "muzzle": {
        "炮口.002",
    },
    "core": {
        "核心.002",
    },
}


def split(source: Path, handedness: str) -> None:
    lines = source.read_text(encoding="utf-8").splitlines()

    # 液压杆材质由多个互不相连的小零件组成；只把最外端的连接头拆出来，
    # 杆身仍留在 static，避免整根液压杆跟着盖板移动。
    hydraulic_head_faces = {"upper_rod_head": set(), "lower_rod_head": set()}
    vertices = [None] + [
        tuple(map(float, line.split()[1:4]))
        for line in lines
        if line.startswith("v ")
    ]
    for material, target, predicate in (
        ("液压杆1.002", "upper_rod_head", lambda center: center[1] > 0.55),
        ("液压杆2.002", "lower_rod_head", lambda center: center[1] < 0.35),
    ):
        hydraulic_faces = []
        current_material = None
        for line_index, line in enumerate(lines):
            if line.startswith("usemtl "):
                current_material = line[7:].strip()
            elif line.startswith("f ") and current_material == material:
                indices = tuple(int(token.split("/")[0]) for token in line.split()[1:])
                hydraulic_faces.append((line_index, indices))
        linked = {}
        for face_index, (_, indices) in enumerate(hydraulic_faces):
            for vertex in indices:
                linked.setdefault(vertex, []).append(face_index)
        seen = set()
        for start in range(len(hydraulic_faces)):
            if start in seen:
                continue
            pending = [start]
            seen.add(start)
            component = []
            while pending:
                current = pending.pop()
                component.append(current)
                for vertex in hydraulic_faces[current][1]:
                    for neighbor in linked[vertex]:
                        if neighbor not in seen:
                            seen.add(neighbor)
                            pending.append(neighbor)
            points = [vertices[v] for face in component for v in hydraulic_faces[face][1]]
            minimum = tuple(min(point[axis] for point in points) for axis in range(3))
            maximum = tuple(max(point[axis] for point in points) for axis in range(3))
            center = tuple((minimum[axis] + maximum[axis]) / 2 for axis in range(3))
            if predicate(center):
                hydraulic_head_faces[target].update(
                    hydraulic_faces[face][0] for face in component
                )

    for group_name, materials in GROUPS.items():
        output = []
        current_material = None
        face_count = 0

        for line_index, line in enumerate(lines):
            if line.startswith("usemtl "):
                current_material = line[7:].strip()
                output.append(line)
            elif line.startswith("f "):
                is_head = line_index in hydraulic_head_faces.get(group_name, set())
                is_any_head = any(line_index in faces for faces in hydraulic_head_faces.values())
                is_hydraulic = current_material in {"液压杆1.002", "液压杆2.002"}
                if (current_material in materials and not is_hydraulic) or (is_hydraulic and not is_any_head and group_name == "static") or is_head:
                    output.append(line)
                    face_count += 1
            else:
                output.append(line)

        target = ROOT / f"tendouaris_sword_of_light_{handedness}_{group_name}.obj"
        target.write_text("\n".join(output) + "\n", encoding="utf-8")
        print(f"{target}: {face_count} faces")


if __name__ == "__main__":
    for side, source_path in SOURCES.items():
        split(source_path, side)