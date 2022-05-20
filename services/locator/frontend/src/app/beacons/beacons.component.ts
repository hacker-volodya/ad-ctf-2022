import { Component, OnInit } from '@angular/core';
import {LocatorService} from "../locator.service";

@Component({
  selector: 'app-beacons',
  templateUrl: './beacons.component.html',
  styleUrls: ['./beacons.component.scss']
})
export class BeaconsComponent implements OnInit {
  ids: string[] = [];

  id: string = "";
  lastId: string = "";
  token: string = "";

  constructor(private locatorService: LocatorService) { }

  ngOnInit(): void {
    this.getBeacons();
  }

  getBeacons(): void {
    this.locatorService.getList().subscribe(ids => this.ids = ids);
  }

  create(): void {
    this.lastId = this.id;
    this.locatorService.create(this.id).subscribe(token => this.token = token);
  }
}
