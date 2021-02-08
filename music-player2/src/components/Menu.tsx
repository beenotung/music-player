import React from 'react';
import {
  IonContent,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonListHeader,
  IonMenu,
  IonMenuToggle,
  IonNote,
  IonText,
} from '@ionic/react';

import { useLocation } from 'react-router-dom';
import {
  musicalNotes,
  archiveOutline,
  archiveSharp,
  bookmarkOutline,
  folderOpenOutline,
  folderOpenSharp,
  heartOutline,
  heartSharp,
  mailOutline,
  mailSharp,
  paperPlaneOutline,
  paperPlaneSharp,
  trashOutline,
  trashSharp,
  warningOutline,
  warningSharp,
  folderOutline,
  playOutline,
  playSharp,
  settingsOutline,
  settingsSharp,
  logoGithub,
  shareOutline,
  shareSharp,
} from 'ionicons/icons';
import './Menu.css';

interface MenuItem {
  url: string;
  icon?: string;
  ios?: string;
  md?: string;
  title: string;
}

const appPages: MenuItem[] = [
  {
    title: 'Inbox',
    url: '/page/Inbox',
    ios: mailOutline,
    md: mailSharp,
  },
  {
    title: 'Outbox',
    url: '/page/Outbox',
    ios: paperPlaneOutline,
    md: paperPlaneSharp,
  },
  {
    title: 'Favorites',
    url: '/page/Favorites',
    ios: heartOutline,
    md: heartSharp,
  },
  {
    title: 'Archived',
    url: '/page/Archived',
    ios: archiveOutline,
    md: archiveSharp,
  },
  {
    title: 'Trash',
    url: '/page/Trash',
    ios: trashOutline,
    md: trashSharp,
  },
  {
    title: 'Spam',
    url: '/page/Spam',
    ios: warningOutline,
    md: warningSharp,
  },
];

const labels = ['Family', 'Friends', 'Notes', 'Work', 'Travel', 'Reminders'];

const Menu: React.FC = () => {
  const location = useLocation();

  const MenuItem = (props: MenuItem) => (
    <IonMenuToggle autoHide={false}>
      <IonItem
        className={location.pathname === props.url ? 'selected' : ''}
        routerLink={props.url}
        routerDirection="none"
        lines="none"
        detail={false}
      >
        <IonIcon slot="start" icon={props.icon} md={props.md} ios={props.ios} />
        <IonLabel>{props.title}</IonLabel>
      </IonItem>
    </IonMenuToggle>
  );

  return (
    <IonMenu contentId="main" type="overlay" className="ion-no-padding">
      <IonContent className="ion-no-padding">
        <IonList
          aria-label="App Information Banner"
          className="ion-no-padding banner"
          color="medium"
        >
          <IonItem color="primary" className="ion-padding-vertical">
            <IonIcon
              slot="start"
              size="large"
              color="dark"
              icon={musicalNotes}
            />
            <IonText>
              <b aria-label="App Name">Music Player</b>{' '}
              <span aria-label="App Version">(v2.0.0)</span>
              <br />
              by <span aria-label="Author Name">Beeno Tung</span>
            </IonText>
          </IonItem>
        </IonList>
        <IonList>
          <IonListHeader>Functions</IonListHeader>
          <MenuItem
            title="Select Folder"
            ios={folderOpenOutline}
            md={folderOpenSharp}
            url="/folder"
          />

          <MenuItem
            title="Music Player"
            ios={playOutline}
            md={playSharp}
            url="/folder"
          />
          <MenuItem
            title="Settings"
            ios={settingsOutline}
            md={settingsSharp}
            url="/folder"
          />
        </IonList>
        <IonList>
          <IonListHeader>Communicate </IonListHeader>
          <MenuItem title="About" icon={logoGithub} url="/folder" />
          <MenuItem
            title="Share"
            ios={shareOutline}
            md={shareSharp}
            url="/folder"
          />
          <MenuItem
            title="Feedback"
            ios={paperPlaneOutline}
            md={paperPlaneSharp}
            url="/folder"
          />
        </IonList>
     </IonContent>
    </IonMenu>
  );
};

export default Menu;
