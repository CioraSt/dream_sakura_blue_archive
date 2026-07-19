from pathlib import Path


SOURCE = Path("src/main/resources/assets/dream_sakura_blue_archive/models/obj/tendouaris_sword_of_light.obj")
MATERIAL = "盖板.002"


lines = SOURCE.read_text(encoding="utf-8").splitlines()
vertices = [None] + [
    tuple(map(float, line.split()[1:4]))
    for line in lines
    if line.startswith("v ")
]

faces = []
material = None
for line_number, line in enumerate(lines):
    if line.startswith("usemtl "):
        material = line[7:].strip()
    elif line.startswith("f ") and material == MATERIAL:
        indices = tuple(int(token.split("/")[0]) for token in line.split()[1:])
        faces.append((line_number, indices))

faces_by_vertex = {}
for face_index, (_, indices) in enumerate(faces):
    for vertex_index in indices:
        faces_by_vertex.setdefault(vertex_index, []).append(face_index)

seen = set()
components = []
for start in range(len(faces)):
    if start in seen:
        continue
    pending = [start]
    seen.add(start)
    component_faces = []
    while pending:
        current = pending.pop()
        component_faces.append(current)
        for vertex_index in faces[current][1]:
            for neighbor in faces_by_vertex[vertex_index]:
                if neighbor not in seen:
                    seen.add(neighbor)
                    pending.append(neighbor)

    indices = {vertex for face in component_faces for vertex in faces[face][1]}
    positions = [vertices[index] for index in indices]
    minimum = tuple(min(position[axis] for position in positions) for axis in range(3))
    maximum = tuple(max(position[axis] for position in positions) for axis in range(3))
    center = tuple((minimum[axis] + maximum[axis]) / 2.0 for axis in range(3))
    size = tuple(maximum[axis] - minimum[axis] for axis in range(3))
    components.append((component_faces, minimum, maximum, center, size))

print(f"{MATERIAL}: {len(faces)} faces, {len(components)} connected components")
for index, (component_faces, minimum, maximum, center, size) in enumerate(components):
    first_line = min(faces[face][0] + 1 for face in component_faces)
    print(
        f"#{index}: faces={len(component_faces):3d}, first_line={first_line:5d}, "
        f"center={tuple(round(value, 6) for value in center)}, "
        f"size={tuple(round(value, 6) for value in size)}, "
        f"min={tuple(round(value, 6) for value in minimum)}, "
        f"max={tuple(round(value, 6) for value in maximum)}"
    )