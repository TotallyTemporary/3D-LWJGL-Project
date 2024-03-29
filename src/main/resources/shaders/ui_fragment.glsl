#version 330 core
in vec2 pass_textureCoords;
out vec4 FragColor;

uniform sampler2D tex;

void main()
{
    vec4 tex = texture(tex, pass_textureCoords);
    if (tex.a < 0.5f) {
      discard;
    }
    FragColor = tex;
}