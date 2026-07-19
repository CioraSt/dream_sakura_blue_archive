from pathlib import Path

source = Path("src/main/resources/assets/dream_sakura_blue_archive/models/obj/tendouaris_sword_of_light.obj")
lines = source.read_text(encoding="utf-8").splitlines()
vertices = [None] + [tuple(map(float, line.split()[1:4])) for line in lines if line.startswith("v ")]

for wanted in ("液压杆1.002", "液压杆2.002"):
    material = None
    faces = []
    for line_no, line in enumerate(lines):
        if line.startswith("usemtl "):
            material = line[7:].strip()
        elif line.startswith("f ") and material == wanted:
            faces.append((line_no, tuple(int(token.split("/")[0]) for token in line.split()[1:])))

    by_vertex = {}
    for face_no, (_, indices) in enumerate(faces):
        for vertex in indices:
            by_vertex.setdefault(vertex, []).append(face_no)

    seen = set()
    components = []
    for start in range(len(faces)):
        if start in seen:
            continue
        pending = [start]
        seen.add(start)
        component = []
        while pending:
            current = pending.pop()
            component.append(current)
            for vertex in faces[current][1]:
                for neighbor in by_vertex[vertex]:
                    if neighbor not in seen:
                        seen.add(neighbor)
                        pending.append(neighbor)
        indices = {vertex for face in component for vertex in faces[face][1]}
        positions = [vertices[index] for index in indices]
        minimum = tuple(min(p[axis] for p in positions) for axis in range(3))
        maximum = tuple(max(p[axis] for p in positions) for axis in range(3))
        center = tuple((minimum[axis] + maximum[axis]) / 2 for axis in range(3))
        components.append((len(component), center, minimum, maximum))

    print(wanted, "faces=", len(faces), "components=", len(components))
    for component in sorted(components, reverse=True):
        print(" ", component)