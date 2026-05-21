export interface InfoResponse {
  'display-ribbon-on-profiles'?: string;
  'keycloak-admin-url'?: string;
  git?: any;
  build?: any;
  activeProfiles?: string[];
}

export class ProfileInfo {
  constructor(
    public activeProfiles?: string[],
    public ribbonEnv?: string,
    public inProduction?: boolean,
    public openAPIEnabled?: boolean,
    public keycloakAdminUrl?: string,
  ) {}
}
