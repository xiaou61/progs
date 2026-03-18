export function buildMenus(codes: string[]) {
  return [
    ...(codes.includes('USER_MANAGE') ? ['users'] : []),
    ...(codes.includes('ROLE_MANAGE') ? ['roles'] : []),
    ...(codes.includes('CAMPUS_MANAGE') ? ['campuses'] : [])
  ]
}
